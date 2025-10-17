package charging_manage_be.services.users;

import charging_manage_be.model.dto.user.UserRequest;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class UserServiceImpl implements UserService {



    @Autowired
    private UserRepository userRepository;


    private int characterLength = 5;
    private int numberLength = 5;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (userRepository.existsById(newId));
        return newId;
    }

    @Override
    public UserEntity saveUser(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        else if (user.getUserID() == null) {
            user.setUserID(generateUniqueId());
        }
        else if (userRepository.existsById(user.getUserID())) {
            throw new IllegalArgumentException("User already exists");
        }
        return userRepository.save(user);
    }

    @Override
    public boolean updateUser(String userId, UserRequest userRequest) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        else{
            user.setUserID(userId);
            user.setFirstName(userRequest.getFirstName());
            user.setLastName(userRequest.getLastName());
            user.setBirthDate(userRequest.getBirthDate());
            user.setGender(userRequest.isGender());
            user.setEmail(userRequest.getEmail());
            user.setPassword(userRequest.getPassword());
            user.setPhoneNumber(userRequest.getPhoneNumber());
            user.setRole(userRequest.getRole());
            user.setStatus(userRequest.isStatus());
            user.setCreatedAt(user.getCreatedAt());

            userRepository.save(user);
        }
        return true;
    }

    @Override
    public boolean softDeleteUser(String userID) { // Hàm xóa này chỉ là xóa mềm, tức là chỉ set status = false
        if (!userRepository.existsById(userID)) {
            throw new IllegalArgumentException("User does not exist");
        } else {
            UserEntity user = userRepository.findById(userID).get();
            user.setStatus(false);
            userRepository.save(user);
            return true;
        }
    }

    @Override
    public Optional<UserEntity> getUserByID(String userID) {
        if (!userRepository.existsById(userID)) {
            throw new IllegalArgumentException("User does not exist");
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

    @Override
    public UserEntity loginUser(String email, String password) {
        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            if (user.getPassword().equals(password) && user.isStatus()) {
                return user;
            }
            return null;
        }
        return null;
    }

    @Override
    public UserEntity registerUser(UserEntity userEntity) {
        Optional<UserEntity> optionalUser = userRepository.findByEmail(userEntity.getEmail());
        if (optionalUser.isPresent()) {
            throw  new IllegalArgumentException("User already exists");
        }
        UserEntity newUser = new UserEntity();
        newUser.setUserID(generateUniqueId());
        newUser.setEmail(userEntity.getEmail());
        newUser.setPassword(userEntity.getPassword());
        newUser.setFirstName(userEntity.getFirstName());
        newUser.setLastName(userEntity.getLastName());
        newUser.setBirthDate(userEntity.getBirthDate());
        newUser.setGender(userEntity.isGender());
        newUser.setPhoneNumber(userEntity.getPhoneNumber());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setRole("DRIVER"); // Mặc định user mới tạo có role là USER
        newUser.setStatus(true); // Mặc định user mới tạo có trạng thái active
        return userRepository.save(newUser);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void saveUserTemp(UserEntity userEntity) {
        try {
            String userJson = objectMapper.writeValueAsString(userEntity);  // Serialize UserEntity thành JSON
            redisTemplate.opsForValue().set("user:" + userEntity.getEmail(), userJson, 10, TimeUnit.MINUTES);  // Lưu vào Redis với thời gian hết hạn là 10 phút cho việc lưu tạm thời trước khi xác thực OTP
            System.out.println("User saved to Redis: " + userEntity.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserEntity getTempUserByEmail(String email) {
        String userJson = redisTemplate.opsForValue().get("user:" + email); // Lấy dữ liệu từ Redis
        try {
            if (userJson != null) {
                return objectMapper.readValue(userJson, UserEntity.class);  // Deserialize JSON thành đối tượng UserEntity
            }
            else{
                System.out.println("User not found in Redis for email: " + email);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;  // Trả về null nếu không tìm thấy hoặc có lỗi khi chuyển đổi
    }

    @Override
    public List<UserEntity> getUserByRole(String role) {
        return userRepository.findAllByRole(role);
    }
    @Override
    public UserEntity getUserEmail(String email) {
          return userRepository.findByEmail(email).orElse(null);
    }


}
