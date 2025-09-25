package charging_manage_be.services.charging_session;

import charging_manage_be.model.entity.Charging.ChargingSessionEntity;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.repository.charging_session.ChargingSessionRepository;
import charging_manage_be.services.payments.PaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class ChargingSessionServiceImpl {
    private final int characterLength = 5;
    private final int numberLength = 4;
    @Autowired
    private ChargingSessionRepository chargingSession;
    private PaymentServiceImpl paymentService;
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
    public boolean addSession(ChargingSessionEntity session) {
        try {
            if(session == null) {
                return false;
            }
            session.setChargingSessionId(generateUniqueId());
            chargingSession.save(session);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
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
    public BigDecimal calculateAmount(ChargingSessionEntity session) {
        // lấy giá của trụ sạc và thời gian sạc để tính tiền
        var rate = session.getChargingPost().getChargingFeePerKWh();
        var duration = java.time.Duration.between(session.getStartTime(), session.getEndTime()).toHours();
        return rate.multiply(BigDecimal.valueOf(duration));
    }
    public boolean endSession(ChargingSessionEntity session) {
        try {
            session.setDone(true);
            session.setEndTime(LocalDateTime.now());
            session.setTotalAmount(calculateAmount(session));
            updateSession(session);
            // gọi hóa đơn và tính tiền từ trụ sạc
            PaymentEntity payment = new PaymentEntity();
            payment.setUser(session.getUser());
            payment.setChargingSessionId(session.getChargingSessionId());
            payment.setPrice(session.getTotalAmount());
            paymentService.addPayment(payment);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
