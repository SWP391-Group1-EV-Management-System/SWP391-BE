package charging_manage_be.repository.user_reputations;

import charging_manage_be.model.entity.reputations.UserReputationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserReputationRepository extends JpaRepository<UserReputationEntity, Integer> {
    List<UserReputationEntity> findByUser_UserID(String userID);
    Optional<UserReputationEntity> findFirstByUser_UserIDOrderByCreatedAtDesc(String userID);
}

