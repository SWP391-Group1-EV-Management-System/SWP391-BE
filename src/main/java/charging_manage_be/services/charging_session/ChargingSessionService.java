package charging_manage_be.services.charging_session;

import charging_manage_be.model.entity.charging.ChargingSessionEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ChargingSessionService {
    // khi driver quẹt QR thì sẽ lấy thông tin userId, carId, và lấy booking nếu có để tạo session
     boolean addSessionWithBooking(String bookingId, LocalDateTime expectedEndTime);
     boolean addSessionWithoutBooking(String userId,String postId, LocalDateTime expectedEndTime);
     boolean updateSession(ChargingSessionEntity session);
     BigDecimal calculateAmount(ChargingSessionEntity session) ;
     boolean endSession(String sessionId);
    ChargingSessionEntity getSessionById(String sessionId);
    LocalDateTime getExpectedEndTime(String post);
    List<ChargingSessionEntity> findSessionsToEnd(LocalDateTime currentTime);
    List<ChargingSessionEntity> getAllSessionByUserStatusDone(String userId);
    List<ChargingSessionEntity> getAllSessionInStationWithStatus(String stationId, boolean isDone);
    List<ChargingSessionEntity> getAllSessions();
}
