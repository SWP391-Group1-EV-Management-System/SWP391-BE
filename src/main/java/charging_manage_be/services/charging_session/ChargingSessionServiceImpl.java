package charging_manage_be.services.charging_session;

import charging_manage_be.controller.charging.ChargingSession;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    public boolean endSession(String sessionId) {
        ChargingSessionEntity session = getSessionById(sessionId);
        if(session == null)
        {
            return false; // session không tồn tại
        }
        if (session.isDone()) {
            return false; // session đã kết thúc rồi
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
            //PaymentEntity payment = new PaymentEntity();
            paymentService.addPayment(sessionId, null);
            // tìm kiếm waiting tại trụ đó và chuyển họ qua booking
            bookingService.processBooking(session.getChargingPost().getIdChargingPost());

            //deleteProgress(sessionId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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


    // Hàm cập nhật tiến trình sạc real-time (được gọi mỗi giây)
//    @Scheduled(fixedRate = 1000)
//    public void updateChargingProgress() {
//        List<ChargingSessionEntity> activeSessions = chargingSession.findByIsDoneFalse();
//
//        for (ChargingSessionEntity session : activeSessions) {
//            LocalDateTime now = LocalDateTime.now();
//            LocalDateTime start = session.getStartTime();
//            if (start == null || now.isBefore(start)){
//                continue;
//            }
//
//            long elapsedSeconds = Duration.between(start.getSecond, now)s(;) // Lấy khoảng thời gian giữa 2 thời điểm và quy đổi ra giây
//
//            double power = session.getChargingPost().getMaxPower().doubleValue();
//            double energyCharged = (power * elapsedSeconds) / 3600.0; // kWh đã sạc được
//            updateProgress(session.getChargingSessionId(), energyCharged, elapsedSeconds);
//        }
//    }
//    @Scheduled(fixedRate = 1000)
//    public void updateChargingProgress() {
//        List<ChargingSessionEntity> activeSessions = chargingSession.findByIsDoneFalse();
//
//        for (ChargingSessionEntity session : activeSessions) {
//            LocalDateTime now = LocalDateTime.now();
//            LocalDateTime start = session.getStartTime();
//            if (start == null || now.isBefore(start)){
//                continue;
//            }
//
//            long elapsedSeconds = Duration.between(start, now).getSeconds();
//
//            double power = session.getChargingPost().getMaxPower().doubleValue();
//            double energyCharged = (power * elapsedSeconds) / 3600.0;
//            int pin = carService.pinRandom();
//            int minuteMax = carService.maxMinutes(pin);
//            Map<Object, Object> progress = redisTemplate.opsForHash().entries("charging:session:" + session.getChargingSessionId());
//            Object pinObj = progress.get("pin");
//            Object minuteMaxObj = progress.get("minuteMax");
//            if(pinObj != null && minuteMaxObj != null) {
//                pin = (Integer) pinObj;
//                minuteMax = (Integer) minuteMaxObj;
//            }
//            // Tính pin dựa trên thời gian (tăng mỗi 13.25 giây)
//            int pinIncrements = (int) (elapsedSeconds / 13.25);
//            int currentPin = Math.min(pin + pinIncrements, 100); // giới hạn tối đa 100%
//
//            // Tính minuteMax còn lại (giảm mỗi phút)
//            int minutesElapsed = (int) (elapsedSeconds / 60);
//            int remainingMinutes = Math.max(minuteMax - minutesElapsed, 0); // không âm
//
//            updateProgress(session.getChargingSessionId(), energyCharged, elapsedSeconds, currentPin, remainingMinutes);
//        }
//    }
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

            String key = "charging:session:" + session.getChargingSessionId();
            Map<Object, Object> progress = redisTemplate.opsForHash().entries(key);

            int initialPin;
            int initialMinuteMax;

            // Kiểm tra và khởi tạo giá trị ban đầu nếu chưa có
            if (progress.isEmpty() || !progress.containsKey("initialPin")) {
                initialPin = carService.pinRandom();
                initialMinuteMax = carService.maxMinutes(initialPin);

                Map<String, String> initMap = new HashMap<>();
                initMap.put("initialPin", String.valueOf(initialPin));
                initMap.put("initialMinuteMax", String.valueOf(initialMinuteMax));
                redisTemplate.opsForHash().putAll(key, initMap);
            } else {
                initialPin = Integer.parseInt(progress.get("initialPin").toString());
                initialMinuteMax = Integer.parseInt(progress.get("initialMinuteMax").toString());
            }

            // Tính pin dựa trên giá trị ban đầu (tăng mỗi 13.25 giây)
            int pinIncrements = (int) (elapsedSeconds / 13.25);
            int currentPin = Math.min(initialPin + pinIncrements, 100);

            // Tính minuteMax còn lại (giảm mỗi phút)
            int minutesElapsed = (int) (elapsedSeconds / 60);
            int remainingMinutes = Math.max(initialMinuteMax - minutesElapsed, 0);

            updateProgress(session.getChargingSessionId(), energyCharged, elapsedSeconds, currentPin, remainingMinutes);
        }
    }

    // Update quá trình dô Redis
    private void updateProgress(String sessionId, double energyCharged, long elapsedSeconds, int pin, int minuteMax) {
        String key = "charging:session:" + sessionId;
        Map<String, String> map = new HashMap<>();
        map.put("chargedEnergy_kWh", String.format( Locale.US, "%.2f", energyCharged));
        map.put("elapsedSeconds", String.valueOf(elapsedSeconds));
        map.put("pin", String.valueOf(pin));
        map.put("minuteMax", String.valueOf(minuteMax));
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
