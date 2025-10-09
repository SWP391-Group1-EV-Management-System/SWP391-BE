package charging_manage_be.model.entity.reputations;

import charging_manage_be.model.entity.users.UserEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table (name = "user_reputations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReputationEntity {
    @Id
    // Làm cho userReputationID tự động random 4 chữ cái và 4 chữ số
    @Column(name = "user_reputation_id", length = 8)
    private String userReputationID;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "level_id", nullable = false)
    private ReputationLevelEntity reputationLevel;

    @Column(name = "current_score", nullable = false)
    private int currentScore;

    @Column (name = "notes", length = 255)
    private String notes;

    @Column (name = "created_at", updatable = false , nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt; // Mặc định là thời gian hiện tại
}
