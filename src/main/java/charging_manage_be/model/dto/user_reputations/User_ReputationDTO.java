package charging_manage_be.model.dto.user_reputations;

import java.time.LocalDateTime;

public class User_ReputationDTO {
    private String levelName; // Được lấy từ Reputation_Level
    private int maxWaitMinutes;   // Được lấy từ Reputation_Level
    private String description;
    private LocalDateTime createdAt; // LocalDateTime thay vì Date là để lấy giờ hiện tại dễ dàng hơn

    public User_ReputationDTO() {
    }

    public User_ReputationDTO(String levelName, int maxWaitMinutes, String description, LocalDateTime createdAt) {
        this.levelName = levelName;
        this.maxWaitMinutes = maxWaitMinutes;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public int getMaxWaitMinutes() {
        return maxWaitMinutes;
    }

    public void setMaxWaitMinutes(int maxWaitMinutes) {
        this.maxWaitMinutes = maxWaitMinutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
