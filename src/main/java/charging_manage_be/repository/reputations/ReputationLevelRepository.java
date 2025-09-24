package charging_manage_be.repository.reputations;

import charging_manage_be.model.entity.reputations.ReputationLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReputationLevelRepository extends JpaRepository<ReputationLevelEntity,Integer> {

}
