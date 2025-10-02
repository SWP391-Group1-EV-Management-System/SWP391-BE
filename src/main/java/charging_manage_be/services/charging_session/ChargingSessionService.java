package charging_manage_be.services.charging_session;

import charging_manage_be.model.entity.charging.ChargingSessionEntity;

import java.math.BigDecimal;

public interface ChargingSessionService {
    public boolean isExistById(String sessionId);
    public boolean addSessionWithBooking(String bookingId);
    public boolean addSessionWithoutBooking(String userId,String postId);
    public boolean updateSession(ChargingSessionEntity session);
    public BigDecimal calculateAmount(ChargingSessionEntity session);
    public boolean endSession(String sessionId);
}
