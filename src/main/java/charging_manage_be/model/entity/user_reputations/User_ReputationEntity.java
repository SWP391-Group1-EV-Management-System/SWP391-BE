package charging_manage_be.model.entity.user_reputations;

import charging_manage_be.model.entity.reputation_levels.Reputation_LevelEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

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

    @Column (name = "created_at", nullable = false)
    @CreationTimestamp
    private Date createdAt;
}
