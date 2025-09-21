package charging_manage_be.services.users;

import charging_manage_be.model.entity.users.UserEntity;
import java.util.List;

public interface UserService {
    boolean addUser(UserEntity user);
    boolean updateUser(UserEntity user);
    boolean deleteUser(String userID);
    UserEntity getUserByID(String userID);
    List<UserEntity> getAllUsers();
}