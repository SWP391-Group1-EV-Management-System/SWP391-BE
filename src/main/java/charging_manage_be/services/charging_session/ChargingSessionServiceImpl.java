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
import charging_manage_be.services.charging_post.ChargingPostService;
import charging_manage_be.services.charging_station.ChargingStationService;
import charging_manage_be.services.payments.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
@RequiredArgsConstructor
public class ChargingSessionServiceImpl  implements ChargingSessionService {
    private final int characterLength = 5;
    private final int numberLength = 4;

    private final ChargingSessionRepository chargingSession;
    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ChargingPostRepository chargingPostRepository;
    private final ChargingPostService ChargingPostService;
    private final ChargingStationService stationService;

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
    public boolean addSessionWithBooking(String bookingId, LocalDateTime expectedEndTime) {
        try {
            Optional<BookingEntity> optionalBooking = bookingRepository.findById(bookingId);
            if (optionalBooking.isEmpty()) {
                return false;
            }

            BookingEntity booking = optionalBooking.get();
        ChargingSessionEntity session = new ChargingSessionEntity();
        session.setChargingSessionId(generateUniqueId());
        session.setUser(booking.getUser());// trạm trụ trạng thái KWh tổng tiền
        session.setBooking(booking);
        UserEntity userManager = booking.getChargingStation().getUserManager();
        session.setUserManage(userManager);
        session.setStation(booking.getChargingStation());
        session.setChargingPost(booking.getChargingPost());
        session.setKWh(booking.getChargingPost().getChargingFeePerKWh());
        session.setExpectedEndTime(expectedEndTime);
        chargingSession.save(session);
        return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // phải add thời gian dự kiến sạc vào session
    @Override
    public boolean addSessionWithoutBooking(String userId,String postId, LocalDateTime expectedEndTime)
    {
        try {

        Optional<UserEntity> optional = userRepository.findById(userId);
        if (optional.isEmpty()) {
            return false;
        }
            Optional<ChargingPostEntity> optional2 = chargingPostRepository.findById(postId);
            if (optional2.isEmpty()) {
                return false;
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
            session.setKWh(post.getChargingFeePerKWh());
            session.setExpectedEndTime(expectedEndTime);
            chargingSession.save(session);
        return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
        var rate = session.getChargingPost().getChargingFeePerKWh();
        var duration = Duration.between(session.getStartTime(), session.getEndTime()).toMinutes();
        return rate.multiply(BigDecimal.valueOf(duration));
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
            session.setDone(true);
            session.setEndTime(LocalDateTime.now());
            session.setTotalAmount(calculateAmount(session));
            updateSession(session);
            // gọi hóa đơn và tính tiền từ trụ sạc
            //PaymentEntity payment = new PaymentEntity();
            paymentService.addPayment(sessionId);
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
