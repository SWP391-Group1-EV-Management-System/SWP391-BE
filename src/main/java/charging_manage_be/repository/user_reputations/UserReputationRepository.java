package charging_manage_be.repository.user_reputations;

import charging_manage_be.model.entity.reputations.ReputationLevelEntity;
import charging_manage_be.model.entity.reputations.UserReputationEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserReputationRepository extends JpaRepository<UserReputationEntity, String> {
    List<UserReputationEntity> findByUser_UserID(String userID);
    Optional<UserReputationEntity> findFirstByUser_UserIDOrderByCreatedAtDesc(String userID);
    // Tạo một phương thức để lấy level Id dựa trên currentScore trên user_reputations và minScore, maxScore trên reputation_levels
    // Câu query được tạo ra là:
    // SELECT * FROM user_reputations ur
    // JOIN reputation_levels rl ON ur.current_score BETWEEN rl.min_score AND rl.max_score
    // WHERE ur.user_id = ?
    // Hoặc có thể dùng:






    // Câu query được tạo ra là:
    // SELECT * FROM user_reputation WHERE user_id = ? ORDER BY created_at DESC LIMIT 1


    // Hoặc là có thể dùng @Query để viết câu query thủ công như:

    // @Query("SELECT u FROM UserReputationEntity u WHERE u.user.userID = :userID ORDER BY u.createdAt DESC")
    // Optional<UserReputationEntity> findFirstByUser_UserIDOrderByCreatedAtDesc(@Param("userID") String userID);
}

