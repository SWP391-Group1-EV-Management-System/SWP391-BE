package charging_manage_be.services.charging_session;

import charging_manage_be.controller.charging.ChargingSession;
import charging_manage_be.model.dto.charging_session.EndSessionResponseDTO;
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.repository.charging_post.ChargingPostRepository;
import charging_manage_be.repository.charging_session.ChargingSessionRepository;
import charging_manage_be.repository.users.UserRepository;
import charging_manage_be.services.booking.BookingService;
import charging_manage_be.services.car.CarService;
import charging_manage_be.services.charging_post.ChargingPostService;
import charging_manage_be.services.charging_station.ChargingStationService;
import charging_manage_be.services.payments.PaymentService;
import charging_manage_be.services.users.UserService;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
@RequiredArgsConstructor
public class ChargingSessionServiceImpl  implements ChargingSessionService {
    private final int characterLength = 5;
    private final int numberLength = 4;

    @Autowired
    private  ChargingSessionRepository chargingSession;
    @Autowired
    private  PaymentService paymentService;
    @Lazy
    @Autowired
    private  BookingService bookingService;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  ChargingPostRepository chargingPostRepository;
    @Autowired
    private  ChargingPostService ChargingPostService;
    @Autowired
    private  ChargingStationService stationService;
    @Autowired
    private  UserService userService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CarService carService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public boolean isExistById(String sessionId) {
        return chargingSession.existsById(sessionId);
    }
    private String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (isExistById(newId));
        return newId;
    }
    // phải add thời gian dự kiến sạc vào session
    // khi driver quẹt QR thì sẽ lấy thông tin userId, carId, và lấy booking nếu có để tạo session
    @Override
    public String addSessionWithBooking(String bookingId, LocalDateTime expectedEndTime) {
        try {
                BookingEntity booking = bookingService.getBookingByBookingId(bookingId);
            if (booking == null) {
                return null;
            }
        ChargingSessionEntity session = new ChargingSessionEntity();
        session.setChargingSessionId(generateUniqueId());
        session.setUser(booking.getUser());// trạm trụ trạng thái KWh tổng tiền
        session.setBooking(booking);
        UserEntity userManager = booking.getChargingStation().getUserManager();
        session.setUserManage(userManager);
        session.setStation(booking.getChargingStation());
        session.setChargingPost(booking.getChargingPost());
        session.setKWh(BigDecimal.valueOf(0)); // Lưu ý là cái này khi tạo session thì nó phải là 0, khi nào sạc xong thì mới update nó lên bằng số tiền được tính bằng công thức ở dưới
        session.setExpectedEndTime(expectedEndTime);
        // gọi qua cho webscoket
        chargingSession.save(session);
        return session.getChargingSessionId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // phải add thời gian dự kiến sạc vào session
    @Override
    public String addSessionWithoutBooking(String userId,String postId, LocalDateTime expectedEndTime)
    {
        try {

        Optional<UserEntity> optional = userRepository.findById(userId);
        if (optional.isEmpty()) {
            return null;
        }
            Optional<ChargingPostEntity> optional2 = chargingPostRepository.findById(postId);
            if (optional2.isEmpty()) {
                return null;
            }
            ChargingPostEntity post = optional2.get();
            UserEntity user = optional.get();

            ChargingSessionEntity session = new ChargingSessionEntity();
            session.setChargingSessionId(generateUniqueId());
            session.setUser(user);// trạm trụ trạng thái KWh tổng tiền
            UserEntity userManager = post.getChargingStation().getUserManager();
            session.setUserManage(userManager);
            session.setStation(post.getChargingStation());
            session.setChargingPost(post);
            session.setKWh(BigDecimal.valueOf(0));
            session.setExpectedEndTime(expectedEndTime);
            chargingSession.save(session);
        return session.getChargingSessionId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateSession(ChargingSessionEntity session) {
        try {
            if (session == null || !isExistById(session.getChargingSessionId())) {
                return false;
            }
            chargingSession.save(session);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public BigDecimal calculateAmount(ChargingSessionEntity session) {
        // lấy giá của trụ sạc và thời gian sạc để tính tiền
        var rate = session.getKWh();
        return BigDecimal.valueOf(3858).multiply(rate);
    }

    @Override
    public EndSessionResponseDTO endSession(String sessionId) {
        ChargingSessionEntity session = getSessionById(sessionId);

        // Build response DTO
        EndSessionResponseDTO response = EndSessionResponseDTO.builder()
                .sessionId(sessionId)
                .success(false)
                .hasWaitingDriver(false)
                .sentEarlyOffer(false)
                .build();

        if(session == null)
        {
            response.setMessage("Session không tồn tại");
            return response;
        }
        if (session.isDone()) {
            response.setMessage("Session đã kết thúc rồi");
            return response;
        }
        try {
            Map<Object, Object> progress = redisTemplate.opsForHash().entries("charging:session:" + sessionId);
            double chargedEnergy = 0.0;
            if (progress.containsKey("chargedEnergy_kWh")) {
                chargedEnergy = Double.parseDouble(progress.get("chargedEnergy_kWh").toString().replace(",","."));
            }
            session.setKWh(BigDecimal.valueOf(chargedEnergy));
            session.setDone(true);
            session.setEndTime(LocalDateTime.now());
            session.setTotalAmount(calculateAmount(session));
            updateSession(session);
            // gọi hóa đơn và tính tiền từ trụ sạc
            paymentService.addPayment(sessionId, null);

            // Cập nhật response với thông tin session
            response.setChargedEnergy(chargedEnergy);
            response.setTotalAmount(session.getTotalAmount().doubleValue());
            response.setActualEndTime(session.getEndTime());
            response.setExpectedEndTime(session.getExpectedEndTime());

            // xử lý 2 trường hợp cho waitingList
            String postId = session.getChargingPost().getIdChargingPost();
            LocalDateTime expectedEndTime = session.getExpectedEndTime();
            LocalDateTime actualEndTime = session.getEndTime();

            // c1: A RÚT SẠC SỚM → Hỏi B có muốn sạc ngay hay đợi đúng giờ
            // c2: Đến đúng giờ (session tự động end) → B tự động vào booking

            if (expectedEndTime != null && actualEndTime.isBefore(expectedEndTime)) {
                // case 1: A RÚT SẠC SỚM → Gửi notification hỏi driver B
                String nextDriverId = redisTemplate.opsForList().index("queue:post:" + postId, 0);

                if (nextDriverId != null && !nextDriverId.isEmpty()) {
                    // Tính thời gian còn lại phải chờ
                    long minutesRemaining = java.time.Duration.between(actualEndTime, expectedEndTime).toMinutes();

                    // Trim và remove quotes nếu có
                    nextDriverId = nextDriverId.trim().replace("\"", "");

                    // Tạo message
                    Map<String, Object> offerData = new HashMap<>();
                    offerData.put("postId", postId);
                    offerData.put("message", "Driver trước đã kết thúc sớm. Bạn có muốn sạc ngay không?");
                    offerData.put("minutesEarly", minutesRemaining);
                    offerData.put("actualEndTime", actualEndTime.toString());
                    offerData.put("expectedEndTime", expectedEndTime.toString());
                    offerData.put("availableNow", true);

                    // Gửi notification cho driver B
                    simpMessagingTemplate.convertAndSendToUser(
                        nextDriverId,
                        "/queue/early-charging-offer",
                        offerData
                    );


                    // ✅ Cập nhật response cho FE biết
                    response.setHasWaitingDriver(true);
                    response.setSentEarlyOffer(true);
                    response.setNextDriverId(nextDriverId);
                    response.setMinutesEarly(minutesRemaining);
                    response.setMessage("Session kết thúc thành công. Đã gửi offer sạc sớm cho driver tiếp theo.");
                } else {
                    System.out.println("✅ No drivers in waiting list for post: " + postId);
                    response.setMessage("Session kết thúc thành công. Không có driver nào trong hàng đợi.");
                }
            } else {
                // CASE 2: ĐÚNG GIỜ (session tự động end) → Tự động chuyển B vào booking
                bookingService.processBooking(postId);
                System.out.println("✅ [CASE 2] Session ended on time - Automatically processing next booking for post: " + postId);
                response.setMessage("Session kết thúc thành công. Driver tiếp theo đã được tự động chuyển vào booking.");
            }

            response.setSuccess(true);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.setMessage("Lỗi khi kết thúc session: " + e.getMessage());
            return response;
        }
    }
    @Override
    public ChargingSessionEntity getSessionById(String sessionId) {
        Optional<ChargingSessionEntity> optional = chargingSession.findById(sessionId);
        return optional.orElse(null);
    }

    @Override
    public LocalDateTime getExpectedEndTime(String chargingPost) {
        ChargingPostEntity post = ChargingPostService.getChargingPostById(chargingPost);
        return chargingSession.findExpectedEndTimeByChargingPostAndIsDone(post, false).orElse(null);
    }

    @Override
    public List<ChargingSessionEntity> findSessionsToEnd(LocalDateTime currentTime) {
        return chargingSession.findByExpectedEndTimeLessThanEqualAndEndTimeIsNull(currentTime);
    }

    @Override
    public List<ChargingSessionEntity> getAllSessionByUserStatusDone(String userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return chargingSession.findByUserAndIsDone(user, true);
    }

    @Override
    public List<ChargingSessionEntity> getAllSessionInStationWithStatus(String stationId, boolean isDone) {
        ChargingStationEntity station = stationService.getStationById(stationId);
        return chargingSession.findByStationAndIsDone(station ,isDone);
    }

    @Override
    public List<ChargingSessionEntity> getAllSessions() {
        return chargingSession.findAll();
    }

    @Override
    public List<ChargingSessionEntity> getAllSessionsByUserId(String userId) {
        UserEntity user = userService.getUserByID(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return chargingSession.findByUser(user);
    }

    @Override
    public List<ChargingSessionEntity> getAllSessionsByStationId(String stationId) {
        ChargingStationEntity station = stationService.getStationById(stationId);
        return chargingSession.findByStation(station);
    }


    // Hàm cập nhật tiến trình sạc real-time (được gọi mỗi giây)
    @Scheduled(fixedRate = 1000)
    public void updateChargingProgress() {
        List<ChargingSessionEntity> activeSessions = chargingSession.findByIsDoneFalse();

        for (ChargingSessionEntity session : activeSessions) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = session.getStartTime();
            if (start == null || now.isBefore(start)){
                continue;
            }

            long elapsedSeconds = Duration.between(start, now).getSeconds();

            double power = session.getChargingPost().getMaxPower().doubleValue();
            double energyCharged = (power * elapsedSeconds) / 3600.0;

            int currentPin;
            int targetPin;
            int maxSeconds;

            // Lấy thông tin từ Redis
            String userKey = "charging:preference:" + session.getUser().getUserID();
            Map<Object, Object> preferenceData = redisTemplate.opsForHash().entries(userKey);

            // Kiểm tra xem entity đã có giá trị chưa (lần đầu tiên)
            Integer sessionInitialPin = session.getInitialPin();
            Integer sessionTargetPin = session.getInitialMinuteMax(); // Tạm dùng field cũ để lưu targetPin

            if (sessionInitialPin == null || sessionTargetPin == null) {
                // Lần đầu tiên: Lấy từ Redis và lưu vào database
                if (!preferenceData.isEmpty() && preferenceData.containsKey("currentPin")) {
                    currentPin = Integer.parseInt(preferenceData.get("currentPin").toString());
                    targetPin = Integer.parseInt(preferenceData.get("targetPin").toString());
                    maxSeconds = Integer.parseInt(preferenceData.get("desiredChargingTimeSeconds").toString());

                    // Lưu vào database
                    session.setInitialPin(currentPin);
                    session.setInitialMinuteMax(targetPin); // Tạm lưu targetPin vào field này
                    updateSession(session);

                    System.out.println("✅ [INIT] Session " + session.getChargingSessionId() +
                        " - CurrentPin: " + currentPin + " → TargetPin: " + targetPin +
                        " - MaxSeconds: " + maxSeconds);
                } else {
                    // Fallback: Nếu Redis mất, random mới
                    currentPin = carService.pinRandom();
                    targetPin = 100;
                    maxSeconds = carService.calculateMaxSeconds(currentPin, targetPin);

                    session.setInitialPin(currentPin);
                    session.setInitialMinuteMax(targetPin);
                    updateSession(session);

                    System.out.println("⚠️ [FALLBACK] Session " + session.getChargingSessionId() +
                        " - Redis empty, using fallback values");
                }
            } else {
                // Đã có trong database, lấy ra dùng
                currentPin = sessionInitialPin;
                targetPin = sessionTargetPin;

                // Lấy maxSeconds từ Redis (vẫn cần)
                if (!preferenceData.isEmpty() && preferenceData.containsKey("desiredChargingTimeSeconds")) {
                    maxSeconds = Integer.parseInt(preferenceData.get("desiredChargingTimeSeconds").toString());
                } else {
                    maxSeconds = carService.calculateMaxSeconds(currentPin, targetPin);
                }
            }

            // Tính pin hiện tại dựa trên thời gian đã trôi qua (tăng mỗi 13.25 giây = 1%)
            int pinIncrements = (int) (elapsedSeconds / 13.25);
            int calculatedCurrentPin = Math.min(currentPin + pinIncrements, targetPin);

            // Tính thời gian còn lại (giảm dần)
            int secondRemaining = Math.max((int)(maxSeconds - elapsedSeconds), 0);

            // Kiểm tra điều kiện dừng
            boolean shouldStop = false;
            String stopReason = "";

            // Điều kiện 1: Đã đạt target PIN
            if (calculatedCurrentPin >= targetPin) {
                shouldStop = true;
                stopReason = "Target PIN reached";
            }

            // Điều kiện 2: Hết thời gian (secondRemaining = 0)
            if (secondRemaining <= 0) {
                shouldStop = true;
                stopReason = "Time limit reached";
            }

            // Cập nhật progress vào Redis (bao gồm secondRemaining)
            updateProgress(session.getChargingSessionId(), energyCharged, elapsedSeconds,
                          calculatedCurrentPin, targetPin, secondRemaining, maxSeconds);

            // Tự động kết thúc session nếu đạt điều kiện
            if (shouldStop) {
                System.out.println("🔴 [AUTO END] Session " + session.getChargingSessionId() +
                    " - Reason: " + stopReason +
                    " - PIN: " + calculatedCurrentPin + "/" + targetPin +
                    " - Time: " + elapsedSeconds + "s/" + maxSeconds + "s (Remaining: " + secondRemaining + "s)");
                endSession(session.getChargingSessionId());
            }
        }
    }

    // Update quá trình dô Redis
    private void updateProgress(String sessionId, double energyCharged, long elapsedSeconds,
                                int pin, int targetPin, int secondRemaining, int maxSeconds) {
        String key = "charging:session:" + sessionId;
        Map<String, String> map = new HashMap<>();
        map.put("chargedEnergy_kWh", String.format(Locale.US, "%.2f", energyCharged));
        map.put("elapsedSeconds", String.valueOf(elapsedSeconds));
        map.put("pin", String.valueOf(pin));
        map.put("targetPin", String.valueOf(targetPin));
        map.put("secondRemaining", String.valueOf(secondRemaining));
        map.put("maxSeconds", String.valueOf(maxSeconds));
        redisTemplate.opsForHash().putAll(key, map);
    }

    // Lấy tiến trình hiện tại
    @Override
    public Map<Object, Object> getProgress(String sessionId) {
        return redisTemplate.opsForHash().entries("charging:session:" + sessionId);
    }

    // Xóa tiến trình khi kết thúc session
    private void deleteProgress(String sessionId) {
        redisTemplate.delete("charging:session:" + sessionId);
    }



    @Override
    public boolean isPostIdleBySession(String postId) {
        ChargingPostEntity post = ChargingPostService.getChargingPostById(postId);
        if (post == null)
        {
            return false;
        }
        ChargingSessionEntity sessionCheck = chargingSession.findFirstByChargingPostAndIsDoneFalse(post);
        if(sessionCheck != null)
        {
            return false;
        }
        return true;
    }

    @Override
    public void storeChargingPreference(String userId, int targetPin, int desiredChargingTimeSeconds) {
        String key = "charging:preference:" + userId;
        redisTemplate.opsForHash().put(key, "targetPin", String.valueOf(targetPin));
        redisTemplate.opsForHash().put(key, "desiredChargingTimeSeconds", String.valueOf(desiredChargingTimeSeconds));
        redisTemplate.opsForHash().put(key, "maxSecond", String.valueOf(desiredChargingTimeSeconds));

        // TTL 30 phút (trường hợp user không bấm sạc)
        redisTemplate.expire(key, 30, java.util.concurrent.TimeUnit.MINUTES);
    }

    @Override
    public Map<Object, Object> getPreferenceFromRedis(String preferenceKey) {
        return redisTemplate.opsForHash().entries(preferenceKey);
    }

    @Override
    public ChargingSessionEntity getNewSessionInPostId(String postId) {
        return chargingSession.findFirstByChargingPost_IdChargingPostAndIsDoneOrderByStartTimeDesc(postId, false);
    }

    @Override
    public BigDecimal getTotalKwhByUserId(String userId) {
        UserEntity user = userService.getUserByID(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return chargingSession.sumFinishedKwhByUser(user);
    }

    @Override
    public int countSessionsByUserIdAndIsDone(String userId) {
        UserEntity user = userService.getUserByID(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return chargingSession.countByUserAndIsDone(user, true);
    }

    @Override
    public int countSessionsByStation(String userId) {
        ChargingStationEntity station = stationService.getStationByUserId(userId);
        return chargingSession.countByStation(station);
    }

    @Override
    public int countSessionIsProgressByStation(String userId) {
        ChargingStationEntity station = stationService.getStationByUserId(userId);
        return chargingSession.countByStationAndIsDone(station, false);
    }

    @Override
    public int countSessionIsDoneByStation(String userId) {
        ChargingStationEntity station = stationService.getStationByUserId(userId);
        return chargingSession.countByStationAndIsDone(station, true);
    }

    @Override
    public BigDecimal getRevenueByStation(String userId) {
        ChargingStationEntity station = stationService.getStationByUserId(userId);
        return chargingSession.sumTotalAmountByStationAndIsDone(station);
    }

    @Override
    public long countSessionsInCurrentMonth() {
        // Phải lấy theo thời gian HIỆN TẠI luôn
        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0); // Đầu tháng này
        LocalDateTime end = start.plusMonths(1).minusSeconds(1); // Cuối tháng này
        return chargingSession.countSessionsInMonth(start, end);
    }

    @Override
    public long countTotalSessions() {
        return chargingSession.count();
    }

//    @Override
//    @Transactional
//    public boolean addExpectedEndTime(String bookingID, LocalDateTime expectedEndTime) {
//        BookingEntity booking = bookingRepository.findById(bookingID).orElse(null);;
//        if(booking == null)
//        {
//            return false;
//        }
//        booking.setExpectedEndTime(expectedEndTime);
//        bookingRepository.save(booking);
//        waitingListService.addExpectedWaitingTime(booking.getChargingPost().getIdChargingPost(), expectedEndTime);
//        return true;
//    }

}
