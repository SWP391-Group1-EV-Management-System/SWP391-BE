package charging_manage_be.services.user_reputations;

import charging_manage_be.model.entity.reputations.UserReputationEntity;

import java.util.List;
import java.util.Optional;

public interface UserReputationService {
    UserReputationEntity saveUserReputation(UserReputationEntity userReputationEntity);
    List<UserReputationEntity> getUserReputationById(String userID);
    List<UserReputationEntity> getAllUserReputations();
    Optional<UserReputationEntity> getCurrentUserReputationById(String userID);

}
