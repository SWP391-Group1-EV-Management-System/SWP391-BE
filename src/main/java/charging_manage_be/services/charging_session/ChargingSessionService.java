package charging_manage_be.services.charging_session;

import charging_manage_be.model.entity.charging.ChargingSessionEntity;

import java.math.BigDecimal;

public interface ChargingSessionService {
    // khi driver quẹt QR thì sẽ lấy thông tin userId, carId, và lấy booking nếu có để tạo session
     boolean addSessionWithBooking(String bookingId);
     boolean addSessionWithoutBooking(String userId,String postId);
     boolean updateSession(ChargingSessionEntity session);
     BigDecimal calculateAmount(ChargingSessionEntity session) ;
     boolean endSession(String sessionId);
}
