package charging_manage_be.services.charging_session;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.payments.PaymentMethodEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.repository.charging_post.ChargingPostRepository;
import charging_manage_be.repository.charging_session.ChargingSessionRepository;
import charging_manage_be.repository.users.UserRepository;
import charging_manage_be.services.booking.BookingService;
import charging_manage_be.services.payments.PaymentService;
import charging_manage_be.services.payments.PaymentServiceImpl;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
@RequiredArgsConstructor
public class ChargingSessionServiceImpl implements ChargingSessionService {
    private final int characterLength = 5;
    private final int numberLength = 4;

    private final ChargingSessionRepository chargingSession;
    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ChargingPostRepository chargingPostRepository;


    @Override
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
    // khi driver quẹt QR thì sẽ lấy thông tin userId, carId, và lấy booking nếu có để tạo session
    @Override
    public boolean addSessionWithBooking(String bookingId) {
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
        chargingSession.save(session);
        return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addSessionWithoutBooking(String userId,String postId)
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
        var duration = java.time.Duration.between(session.getStartTime(), session.getEndTime()).toHours();
        return rate.multiply(BigDecimal.valueOf(duration));
    }

    @Override
    public boolean endSession(String sessionId) {
        Optional<ChargingSessionEntity> optional = chargingSession.findById(sessionId);
        if (optional.isEmpty()) {
            return false;
        }
        ChargingSessionEntity session = optional.get();
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


}
