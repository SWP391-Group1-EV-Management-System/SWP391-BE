package charging_manage_be.services.users;

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
    boolean softDeleteUser(String userID);
    Optional<UserEntity> getUserByID(String userID);
    List<UserEntity> getAllUsers();
    boolean existID(String userID);
}