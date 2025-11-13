package charging_manage_be.services.reputations;

import charging_manage_be.model.dto.reputation.ReputationRequest;
import charging_manage_be.model.entity.reputations.ReputationLevelEntity;

import java.util.List;
import java.util.Optional;

public interface ReputationLevelService {

    ReputationLevelEntity saveReputationLevel(ReputationRequest reputationR);
    ReputationLevelEntity updateReputationLevel(int repuId,ReputationRequest reputationLevelEntity);
    boolean deleteReputationLevelById(int levelId);
    Optional<ReputationLevelEntity> getReputationLevelById(int levelId);
    List<ReputationLevelEntity> getAllReputationLevels();

}
