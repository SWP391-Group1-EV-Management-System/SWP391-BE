package charging_manage_be.model.entity.reputations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "reputation_levels")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReputationLevelEntity {
    @Id
    @Column(name = "level_id")
    private int levelID;

    @Column(name = "level_name", nullable = false, length = 50)
    private String levelName;
    @Column(name = "min_score", nullable = false)
    private int minScore;
    @Column(name = "max_score", nullable = false)
    private int maxScore;
    @Column(name = "max_wait_minutes", nullable = false)
    private int maxWaitMinutes;
    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "reputationLevel")
    @JsonIgnore
    private List<UserReputationEntity> userReputations;
}
