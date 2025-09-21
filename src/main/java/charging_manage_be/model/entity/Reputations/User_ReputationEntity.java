package charging_manage_be.model.entity.Reputations;

import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table (name = "user_reputations")
public class User_ReputationEntity {
    @Id
    // Làm cho userReputationID tự động random 4 chữ cái và 4 chữ số
    private String userReputationID;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "level_id", nullable = false)
    private Reputation_LevelEntity reputationLevel;

    @Column (name = "notes", length = 255)
    private String notes;

    @Column (name = "created_at", updatable = false , nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt; // Mặc định là thời gian hiện tại

    public User_ReputationEntity() {
    }

    public User_ReputationEntity(String userReputationID, UserEntity user, Reputation_LevelEntity reputationLevel, String notes, LocalDateTime createdAt) {
        this.userReputationID = userReputationID;
        this.user = user;
        this.reputationLevel = reputationLevel;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public String getUserReputationID() {
        return userReputationID;
    }

    public void setUserReputationID(String userReputationID) {
        this.userReputationID = userReputationID;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Reputation_LevelEntity getReputationLevel() {
        return reputationLevel;
    }

    public void setReputationLevel(Reputation_LevelEntity reputationLevel) {
        this.reputationLevel = reputationLevel;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
