package charging_manage_be.model.entity.reputation_levels;

import charging_manage_be.model.entity.user_reputations.User_ReputationEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "reputation_levels")
public class Reputation_LevelEntity {
    @Id
    private int levelID;

    @Column(name = "level_name", nullable = false, length = 50)
    private String levelName;
    @Column(name = "max_wait_minutes", nullable = false)
    private int maxWaitMinutes;
    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "level")
    private List<User_ReputationEntity> userReputations;



    public Reputation_LevelEntity() {
    }

    public Reputation_LevelEntity(int levelID, String levelName, int maxWaitMinutes, String description) {
        this.levelID = levelID;
        this.levelName = levelName;
        this.maxWaitMinutes = maxWaitMinutes;
        this.description = description;
    }

    public int getLevelID() {
        return levelID;
    }

    public void setLevelID(int levelID) {
        this.levelID = levelID;
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
}
