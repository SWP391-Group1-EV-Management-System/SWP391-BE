package charging_manage_be.services.users;

import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserEntity saveUser(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return userRepository.save(user);
    }

    @Override
    public UserEntity updateUser(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (!userRepository.existsById(user.getUserID())) {
            throw  new IllegalArgumentException("User does not exist");
        }
        return userRepository.save(user);
    }

    @Override
    public boolean softDeleteUser(String userID) { // Hàm xóa này chỉ là xóa mềm, tức là chỉ set status = false
         if (!userRepository.existsById(userID)) {
             throw  new IllegalArgumentException("User does not exist");
         }
         else{
             UserEntity user = userRepository.findById(userID).get();
             user.setStatus(false);
             userRepository.save(user);
             return true;
         }
    }

    @Override
    public Optional<UserEntity> getUserByID(String userID) {
        if (!userRepository.existsById(userID)) {
            throw  new IllegalArgumentException("User does not exist");
        }
        return userRepository.findById(userID);
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean existID(String userID) {
        return userRepository.existsById(userID);
    }
}
