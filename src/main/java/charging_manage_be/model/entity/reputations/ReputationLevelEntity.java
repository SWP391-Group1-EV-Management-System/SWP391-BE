package charging_manage_be.model.entity.reputations;

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
    @Column(name = "max_wait_minutes", nullable = false)
    private int maxWaitMinutes;
    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "reputationLevel")
    private List<UserReputationEntity> userReputations;
}
