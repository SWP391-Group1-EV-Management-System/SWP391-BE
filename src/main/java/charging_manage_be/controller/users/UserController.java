package charging_manage_be.controller.users;

import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.services.users.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Đánh dấu lớp này là một controller trong Spring MVC và trả về dữ liệu trực tiếp trong phản hồi HTTP
@RequestMapping("/api/users")
// @RequestMapping("/api/users") // Định nghĩa URL cơ sở cho tất cả các phương thức trong controller
// Ví dụ: tất cả các endpoint trong controller này sẽ bắt đầu bằng /api/users, như /api/users/{id}, /api/users/create, v.v.
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Server is running!";
    }

    // CREATE
    @PostMapping
    public boolean addUser(@RequestBody UserEntity user) {
        return userService.addUser(user);
    }

    // UPDATE
    @PutMapping("/{id}")
    public boolean updateUser(@PathVariable("id") String userID, @RequestBody UserEntity user) {
        user.setUserID(userID);
        return userService.updateUser(user);
    }

    // READ (by ID)
    @GetMapping("/{id}")
    public UserEntity getUserByID(@PathVariable("id") String userID) {
        // biến userID sẽ lấy giá trị từ phần {id} trong URL
        return userService.getUserByID(userID);
    }

    // READ (all)
    @GetMapping
    public List<UserEntity> getAllUsers() {
        return userService.getAllUsers();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public boolean deleteUser(@PathVariable("id") String userID) {
        return userService.deleteUser(userID);
    }



}
