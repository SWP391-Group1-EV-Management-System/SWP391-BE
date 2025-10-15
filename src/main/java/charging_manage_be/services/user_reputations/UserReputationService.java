package charging_manage_be.services.user_reputations;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.reputations.UserReputationEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserReputationService {
    UserReputationEntity saveUserReputation(UserReputationEntity userReputationEntity);
    List<UserReputationEntity> getUserReputationById(String userID);
    List<UserReputationEntity> getAllUserReputations();
    Optional<UserReputationEntity> getCurrentUserReputationById(String userID);

//    // Tạo một hàm để tính số điểm bị trừ của user
//    int calculatePenaltyPoints(LocalDateTime startTime, LocalDateTime expectedEndTime, LocalDateTime actualEndTime);
//    // Tạo một hàm để tính số điểm cộng của user
//    int calculateRewardPoints(LocalDateTime startTime, LocalDateTime expectedEndTime, LocalDateTime actualEndTime);
//
//    // Tạo thêm 1 hàm để ghi xuống DB giống như cập nhật uy tín hiện tại của user theo việc sạc xong sớm
//    int deductPoints(String userID, int pointsToDeduct, String notes);
//    // Tạo thêm 1 hàm để ghi xuống DB giống như cập nhật uy tín hiện tại của user theo việc sạc xong đúng giờ hoặc hơn 90% thời gian sạc dự kiến
//    int addPoints(String userID, int pointsToDeduct, String notes);

    // Tạo một hàm chung để tính số điểm trừ hoặc cộng của user
    int calculateReputationPoints(LocalDateTime startTime, LocalDateTime expectedEndTime, LocalDateTime actualEndTime);
    // Tạo một hàm chung để ghi xuống DB giống như cập nhật uy tín hiện tại của user theo việc cho sạc xong đúng giờ hoặc hơn 90% thời gian sạc dự kiến HOẶC sạc xong sớm
    boolean updatePointsUserReputation(String userID, int pointsChange, String notes);


    // Tạo một hàm để trừ điểm theo số điểm bị trừ của user và lưu vào bảng user_reputations
    void handleEarlyUnplugPenalty(ChargingSessionEntity chargingSession);

    // Tạo một hàm để trừ điểm theo việc đến trễ booking
    void handlerExpiredPenalty(BookingEntity bookingEntity);

}
