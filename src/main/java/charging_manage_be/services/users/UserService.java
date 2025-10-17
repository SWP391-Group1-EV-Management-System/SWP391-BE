package charging_manage_be.services.users;

import charging_manage_be.model.dto.user.UserRequest;
import charging_manage_be.model.entity.users.UserEntity;
import java.util.List;
import java.util.Optional;

public interface UserService {
//    boolean addUser(UserEntity user);
//    boolean updateUser(UserEntity user);
//    boolean deleteUser(String userID);
//    UserEntity getUserByID(String userID);
//    List<UserEntity> getAllUsers();

    UserEntity saveUser(UserEntity user); // Phải là kiểu UserEntity vì hàm save trả về entity đã lưu
    boolean updateUser(String userId, UserRequest userRequest);
    boolean softDeleteUser(String userID);
    Optional<UserEntity> getUserByID(String userID);
    List<UserEntity> getAllUsers();
    boolean existID(String userID);
    UserEntity loginUser(String userID, String password);
    UserEntity registerUser(UserEntity userEntity);
    Optional<UserEntity> findByEmail(String email);
    public void saveUserTemp(UserEntity userEntity);
    UserEntity getTempUserByEmail(String email);
    List<UserEntity> getUserByRole(String role);
    UserEntity getUserEmail(String email);
}