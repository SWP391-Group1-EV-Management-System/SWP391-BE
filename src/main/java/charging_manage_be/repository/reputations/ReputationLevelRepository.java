package charging_manage_be.repository.reputations;

import charging_manage_be.model.entity.reputations.ReputationLevelEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReputationLevelRepository extends JpaRepository<ReputationLevelEntity,Integer> {
    @Query("SELECT r FROM ReputationLevelEntity r WHERE :currentScore BETWEEN r.minScore AND r.maxScore")
    ReputationLevelEntity findLevelByScore(@Param("currentScore") int currentScore);
}
