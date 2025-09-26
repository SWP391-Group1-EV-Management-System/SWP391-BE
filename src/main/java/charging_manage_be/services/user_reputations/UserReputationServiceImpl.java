package charging_manage_be.services.user_reputations;

import charging_manage_be.model.entity.reputations.ReputationLevelEntity;
import charging_manage_be.model.entity.reputations.UserReputationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.reputations.ReputationLevelRepository;
import charging_manage_be.repository.user_reputations.UserReputationRepository;
import charging_manage_be.repository.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserReputationServiceImpl implements UserReputationService{

    @Autowired
    private UserReputationRepository userReputationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReputationLevelRepository reputationLevelRepository;


    @Override
    public UserReputationEntity saveUserReputation(UserReputationEntity userReputationEntity) {

        // Kiểm tra và gán userID đã tồn tại trong bảng UserEntity
        UserEntity userEntity = userRepository.findById(userReputationEntity.getUser().getUserID())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userReputationEntity.getUser().getUserID()));
        userReputationEntity.setUser(userEntity);

        // Kiểm tra và gán levelID đã tồn tại trong bảng ReputationLevelEntity
        ReputationLevelEntity levelEntity = reputationLevelRepository.findById(userReputationEntity.getReputationLevel().getLevelID())
                .orElseThrow(() -> new IllegalArgumentException("Reputation Level not found with ID: " + userReputationEntity.getReputationLevel().getLevelID()));
        userReputationEntity.setReputationLevel(levelEntity);

        // Nếu không check trước khi save thì sẽ bị lỗi:
        // org.hibernate.TransientPropertyValueException: object references an unsaved transient instance - save the transient instance before flushing
        // Bởi vì userReputationEntity tham chiếu đến một UserEntity và ReputationLevelEntity chưa được lưu trong cơ sở dữ liệu
        // Nhưng mặc dù đã có UserEntity và ReputationLevelEntity trong DB rồi thì Hibernate vẫn không biết được điều đó và insert một bản ghi mới với userID và levelID đã tồn tại chứ không phải là tham chiếu đến bản ghi đã có trong DB
        // Nên ta phải check và gán lại cho userReputationEntity trước khi save
        return userReputationRepository.save(userReputationEntity);
    }


    @Override
    public List<UserReputationEntity> getUserReputationById(String userID) {
        if (!userRepository.existsById(userID)) {
            throw new IllegalArgumentException("User not found with ID: " + userID);
        }
        return userReputationRepository.findByUser_UserID(userID);
    }

    @Override
    public Optional<UserReputationEntity> getCurrentUserReputationById(String userID) {
        if (!userRepository.existsById(userID)) {
            throw new IllegalArgumentException("User not found with ID: " + userID);
        }
        return userReputationRepository.findFirstByUser_UserIDOrderByCreatedAtDesc(userID);
    }

    @Override
    public List<UserReputationEntity> getAllUserReputations() {
        return userReputationRepository.findAll();
    }


}
