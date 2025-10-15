package charging_manage_be.services.user_reputations;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.reputations.ReputationLevelEntity;
import charging_manage_be.model.entity.reputations.UserReputationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.reputations.ReputationLevelRepository;
import charging_manage_be.repository.user_reputations.UserReputationRepository;
import charging_manage_be.repository.users.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class UserReputationServiceImpl implements UserReputationService{

    @Autowired
    private UserReputationRepository userReputationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReputationLevelRepository reputationLevelRepository;

    private int characterLength = 2;
    private int numberLength = 3;

    public String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (userReputationRepository.existsById(newId));
        return newId;
    }


    @Override
    public UserReputationEntity saveUserReputation(UserReputationEntity userReputationEntity) {

        // Kiểm tra và gán userID đã tồn tại trong bảng UserEntity
        UserEntity userEntity = userRepository.findById(userReputationEntity.getUser().getUserID())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userReputationEntity.getUser().getUserID()));
        userReputationEntity.setUser(userEntity);

        // Kiểm tra và gán levelID đã tồn tại trong bảng ReputationLevelEntity
        ReputationLevelEntity levelEntity = reputationLevelRepository.findLevelByScore(userReputationEntity.getCurrentScore());
        if (levelEntity == null) {
            throw new IllegalArgumentException("No reputation level found for score: " + userReputationEntity.getCurrentScore());
        }

        userReputationEntity.setReputationLevel(levelEntity);

        if (userReputationEntity.getUserReputationID() == null) {
            userReputationEntity.setUserReputationID(generateUniqueId());
        }
        else if (userReputationRepository.existsById(userReputationEntity.getUserReputationID())) {
            throw new IllegalArgumentException("User Reputation already exists");
        }

        return userReputationRepository.save(userReputationEntity);
    }


    @Override
    @Transactional
    public List<UserReputationEntity> getUserReputationById(String userID) {
        if (!userRepository.existsById(userID)) {
            throw new IllegalArgumentException("User not found with ID: " + userID);
        }
        return userReputationRepository.findByUser_UserID(userID);
    }

    @Override
    @Transactional
    public Optional<UserReputationEntity> getCurrentUserReputationById(String userID) {
        if (!userRepository.existsById(userID)) {
            throw new IllegalArgumentException("User not found with ID: " + userID);
        }
        return userReputationRepository.findFirstByUser_UserIDOrderByCreatedAtDesc(userID);
    }

    @Override
    @Transactional
    public int calculateReputationPoints(LocalDateTime startTime, LocalDateTime expectedEndTime, LocalDateTime actualEndTime) {
        if (startTime == null || expectedEndTime == null || actualEndTime == null) {
            throw new IllegalArgumentException("Invalid time values provided.");
        }

        long totalExpectedTime = Duration.between(startTime, expectedEndTime).toMinutes();
        long totalActualTime = Duration.between(startTime, actualEndTime).toMinutes();
        int penaltyPoints = 0;
        int rewardPoints = 0;

        // Lấy mốc thời gian 90% của tổng thời gian sạc dự kiến
        long ninetyPercentageTimeLimitation = (90 * totalExpectedTime)/100;

        // Nếu sạc xong sớm hơn 90% thời gian dự kiến thì sẽ bị trừ điểm theo công thức 90% thời gian - thời gian thực tế chia 5 phút
        if (totalActualTime < ninetyPercentageTimeLimitation) {
            penaltyPoints = (int)Math.ceil((ninetyPercentageTimeLimitation - totalActualTime)/5.0); // Làm tròn lên đối với điểm bị trừ
            return -penaltyPoints;
        }

        // Nếu sạc đúng giờ hoặc trong khoảng 90% thời gian dự kiến thì sẽ được cộng điểm theo công thức (thời gian dự kiến - thời gian thực tế)/10 phút
        else if (totalActualTime >= ninetyPercentageTimeLimitation && totalActualTime <= totalExpectedTime) {
            rewardPoints = (int) Math.floor((totalExpectedTime - totalActualTime)/10.0); // Làm tròn xuống đối với điểm được cộng
            return rewardPoints;

        }

        else if (totalActualTime == totalExpectedTime){
            return 1;
        }

        return 0;
    }

    @Override
    @Transactional
    public boolean updatePointsUserReputation(String userID, int pointsChange, String notes) {
        //  Lấy thông tin uy tín hiện tại của user
        UserReputationEntity currentUser = getCurrentUserReputationById(userID)
                .orElseThrow(() -> new IllegalArgumentException("User reputation not found for user ID: " + userID));
        int currentScore = currentUser.getCurrentScore();
        int newScore = Math.max(0, currentScore + pointsChange); // Tính điểm mới và đảm bảo không âm, nhỏ nhất là 0

        // lấy level tương ứng với điểm mới
        ReputationLevelEntity levelEntity = reputationLevelRepository.findLevelByScore(newScore);
        if (levelEntity == null) {
            throw new IllegalArgumentException("No reputation level found for score: " + newScore);
        }

        UserReputationEntity newUserReputationEntity = new UserReputationEntity();

        newUserReputationEntity.setUserReputationID(generateUniqueId());
        newUserReputationEntity.setCreatedAt(LocalDateTime.now());
        newUserReputationEntity.setUser(currentUser.getUser());
        newUserReputationEntity.setCurrentScore(newScore);
        newUserReputationEntity.setReputationLevel(levelEntity);
        newUserReputationEntity.setNotes(notes);

        userReputationRepository.save(newUserReputationEntity);
        return true;
    }

    @Override
    @Transactional
    public void handleEarlyUnplugPenalty(ChargingSessionEntity chargingSession) {
        if (chargingSession == null ||
                chargingSession.getUser() == null ||
                chargingSession.getStartTime() == null ||
                chargingSession.getEndTime() == null ||
                chargingSession.getExpectedEndTime() == null) {
            throw new IllegalArgumentException("Invalid charging session or user.");
        }

        LocalDateTime startTime = chargingSession.getStartTime();
        LocalDateTime expectedEndTime = chargingSession.getExpectedEndTime();
        LocalDateTime actualEndTime = chargingSession.getEndTime();

        int pointChange  = calculateReputationPoints(startTime, expectedEndTime, actualEndTime);

        // Nếu không có thay đổi điểm thì không cần cập nhật
        // Lấy phần trăm để in ra thông báo
        double percentage = ((Duration.between(startTime, actualEndTime).toMinutes() / (0.9 * Duration.between(startTime, expectedEndTime).toMinutes()) )*100) ;
        String notes = "";
        if (pointChange < 0) {
            notes = "Người dùng đã rút sạc trước " + (int)(100 - percentage) + "% thời gian dự kiến so với mốc 90% thời gian dự kiến, bị trừ " + Math.abs(pointChange) + " điểm uy tín.";
        }
        else if (pointChange > 0){
            notes = "Người dùng đã rút sạc đúng giờ dự kiến hoặc và được hoặc hơn 10' kể từ 90% thời gian dự kiến, được cộng " + pointChange + " điểm uy tín.";
        }
        else{
            notes = "Người dùng tuy không rút sớm nhưng chưa đủ thời gian 10' kể từ 90% của thời gian dự kiến hoặc chưa đủ thời gian dự kiến nên điểm không đổi.";
        }

        updatePointsUserReputation(chargingSession.getUser().getUserID(), pointChange, notes);

    }

    @Override
    @Transactional
    public void handlerExpiredPenalty(BookingEntity bookingEntity) {
        if (bookingEntity == null) {
            throw new IllegalArgumentException("No booking entity found.");
        }
        LocalDateTime createdAt = bookingEntity.getCreatedAt();
        int maxWaitingTime = bookingEntity.getMaxWaitingTime();
        LocalDateTime expiredTime = createdAt.plusMinutes(maxWaitingTime);

        if (LocalDateTime.now().isAfter(expiredTime)) {
            String userId = bookingEntity.getUser().getUserID();
            int penaltyPoint = -30;
            String notes = "User did not arrive within the allowed waiting time, " + Math.abs(penaltyPoint) + " reputation points deducted.";
            updatePointsUserReputation(userId, penaltyPoint, notes);

        }
    }


    @Override
    public List<UserReputationEntity> getAllUserReputations() {
        return userReputationRepository.findAll();
    }
}
