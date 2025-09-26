package charging_manage_be.services.reputations;

import charging_manage_be.model.entity.reputations.ReputationLevelEntity;

import java.util.List;
import java.util.Optional;

public interface ReputationLevelService {

    ReputationLevelEntity saveReputationLevel(ReputationLevelEntity reputationLevelEntity);
    ReputationLevelEntity updateReputationLevel(ReputationLevelEntity reputationLevelEntity);
    boolean deleteReputationLevelById(int levelID);
    Optional<ReputationLevelEntity> getReputationLevelById(int levelID);
    List<ReputationLevelEntity> getAllReputationLevels();

}
