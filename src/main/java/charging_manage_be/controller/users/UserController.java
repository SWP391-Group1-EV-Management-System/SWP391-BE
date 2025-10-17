package charging_manage_be.controller.users;

import charging_manage_be.model.dto.user.UserRequest;
import charging_manage_be.model.dto.user.UserResponse;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserEntity> createUser(@RequestBody UserEntity user) {
        UserEntity savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        UserEntity savedUser = userService.getUserEmail(email);
        UserResponse userResponse = new UserResponse(
                savedUser.getUserID(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getRole(),
                savedUser.getCreatedAt(),
                savedUser.isStatus(),
                savedUser.isGender(),
                savedUser.getPhoneNumber()
        );
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/update/{userID}")
    public ResponseEntity<String> updateUser(@PathVariable String userID, @RequestBody UserRequest userDetails) {
        Optional<UserEntity> findUser = userService.getUserByID(userID);
        if (findUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        else{
            userService.updateUser(userID, userDetails);
            return ResponseEntity.ok("User updated successfully");
        }
    }

    @DeleteMapping("/delete/{userID}")
    public ResponseEntity<String> deleteUser(@PathVariable String userID) { // @PathVariable để lấy userID từ URL
        boolean deleted = userService.softDeleteUser(userID);
        if (deleted) {
            return ResponseEntity.ok("User deleted successfully");
        }
        return ResponseEntity.badRequest().body("Failed to delete user");
    }

    @GetMapping("/getUser/{userID}")
    public ResponseEntity<UserEntity> getUser(@PathVariable String userID) {
        Optional<UserEntity> user = userService.getUserByID(userID);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers().stream()
                .map(savedUser -> new UserResponse(
                        savedUser.getUserID(),
                        savedUser.getEmail(),
                        savedUser.getFirstName(),
                        savedUser.getLastName(),
                        savedUser.getRole(),
                        savedUser.getCreatedAt(),
                        savedUser.isStatus(),
                        savedUser.isGender(),
                        savedUser.getPhoneNumber()
                )).toList();
        return ResponseEntity.ok(users);
    }
    @GetMapping("/getUserByRole/Admin")
    public ResponseEntity<List<UserResponse>> getUserRoleAdmin() {
        List<UserResponse> admins = userService.getUserByRole("ADMIN").stream()
                .map(savedUser -> new UserResponse(
                        savedUser.getUserID(),
                        savedUser.getEmail(),
                        savedUser.getFirstName(),
                        savedUser.getLastName(),
                        savedUser.getRole(),
                        savedUser.getCreatedAt(),
                        savedUser.isStatus(),
                        savedUser.isGender(),
                        savedUser.getPhoneNumber()
                )).toList();
        return ResponseEntity.ok(admins);
    }
    @GetMapping("/getUserByRole/Manager")
    public ResponseEntity<List<UserResponse>> getUserRoleManager() {
        List<UserResponse> admins = userService.getUserByRole("MANAGER").stream()
                .map(savedUser -> new UserResponse(
                        savedUser.getUserID(),
                        savedUser.getEmail(),
                        savedUser.getFirstName(),
                        savedUser.getLastName(),
                        savedUser.getRole(),
                        savedUser.getCreatedAt(),
                        savedUser.isStatus(),
                        savedUser.isGender(),
                        savedUser.getPhoneNumber()
                )).toList();
        return ResponseEntity.ok(admins);
    }
    @GetMapping("/getUserByRole/Staff")
    public ResponseEntity<List<UserResponse>> getUserRoleStaff() {
        List<UserResponse> admins = userService.getUserByRole("STAFF").stream()
                .map(savedUser -> new UserResponse(
                        savedUser.getUserID(),
                        savedUser.getEmail(),
                        savedUser.getFirstName(),
                        savedUser.getLastName(),
                        savedUser.getRole(),
                        savedUser.getCreatedAt(),
                        savedUser.isStatus(),
                        savedUser.isGender(),
                        savedUser.getPhoneNumber()
                )).toList();
        return ResponseEntity.ok(admins);
    }
    @GetMapping("/getUserByRole/Driver")
    public ResponseEntity<List<UserResponse>> getUserRoleDriver() {
        List<UserResponse> admins = userService.getUserByRole("DRIVER").stream()
                .map(savedUser -> new UserResponse(
                        savedUser.getUserID(),
                        savedUser.getEmail(),
                        savedUser.getFirstName(),
                        savedUser.getLastName(),
                        savedUser.getRole(),
                        savedUser.getCreatedAt(),
                        savedUser.isStatus(),
                        savedUser.isGender(),
                        savedUser.getPhoneNumber()
                )).toList();
        return ResponseEntity.ok(admins);
    }

}