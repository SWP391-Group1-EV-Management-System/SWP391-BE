package charging_manage_be.model.dto.user_reputations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User_ReputationDTO {
    private String levelName; // Được lấy từ Reputation_Level
    private int maxWaitMinutes;   // Được lấy từ Reputation_Level
    private String description;
    private LocalDateTime createdAt; // LocalDateTime thay vì Date là để lấy giờ hiện tại dễ dàng hơn
}
