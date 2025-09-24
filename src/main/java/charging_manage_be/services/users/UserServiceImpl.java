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
        return userRepository.save(user);
    }

    @Override
    public boolean softDeleteUser(String userID) {
         try{
                Optional<UserEntity> userFound = userRepository.findById(userID);
                if (!userFound.isEmpty()) { // có thể dùng isPresent()
                    UserEntity user = userFound.get();
                    user.setStatus(false); // Assuming there's an 'active' field to indicate soft deletion
                    userRepository.save(user);
                    return true;
                }
                else {
                    return false; // User not found
                }
            } catch (Exception e) {
                return false; // Handle exception (e.g., log it)
         }
    }

    @Override
    public Optional<UserEntity> getUserByID(String userID) {
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
