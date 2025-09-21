package charging_manage_be.repository.users;

import charging_manage_be.model.entity.users.UserEntity;

import java.util.List;

public interface UserRepository {
    public boolean addUser(UserEntity user);
    public boolean updateUser(UserEntity user);
    public boolean deleteUser(String userID);
    public UserEntity getUserByID(String userID);
    public List<UserEntity> getAllUsers();
}
