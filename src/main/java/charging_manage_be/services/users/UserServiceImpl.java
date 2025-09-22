package charging_manage_be.services.users;

import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.users.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Nên dùng private final UserRepository userRepository hơn là @Autowired UserRepository userRepository
    // Vì khi dùng @Autowired thì userRepository có thể bị null nếu Spring không thể tiêm đúng bean
    // Còn khi dùng constructor injection với private final thì userRepository sẽ luôn được khởi tạo đúng cách khi đối tượng UserServiceImplement được tạo
    // Điều này giúp tránh lỗi NullPointerException khi sử dụng userRepository trong các phương thức


    @Override
    public boolean addUser(UserEntity user) {
        return userRepository.addUser(user);
    }

    @Override
    public boolean updateUser(UserEntity user) {
        return userRepository.updateUser(user);
    }

    @Override
    public boolean deleteUser(String userID) {
        return userRepository.deleteUser(userID);
    }

    @Override
    public UserEntity getUserByID(String userID) {
        return userRepository.getUserByID(userID);
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.getAllUsers();
    }
}
