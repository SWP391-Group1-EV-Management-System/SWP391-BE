package charging_manage_be.services.user_reputations;

import charging_manage_be.model.entity.reputations.UserReputationEntity;
import charging_manage_be.repository.user_reputations.UserReputationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserReputationServiceImpl implements UserReputationService{

    @Autowired
    private UserReputationRepository userReputationRepository;

    @Override
    public UserReputationEntity saveUserReputation(UserReputationEntity userReputationEntity) {
        return userReputationRepository.save(userReputationEntity);
    }

    @Override
    public List<UserReputationEntity> getUserReputationById(String userID) {
        return userReputationRepository.findByUser_UserID(userID);
    }

    @Override
    public List<UserReputationEntity> getAllUserReputations() {
        return userReputationRepository.findAll();
    }

    @Override
    public Optional<UserReputationEntity> getCurrentUserReputationById(String userID) {
        return userReputationRepository.findFirstByUser_UserIDOrderByCreatedAtDesc(userID);
    }
}
