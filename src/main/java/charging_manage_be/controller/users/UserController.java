package charging_manage_be.controller.users;

import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

@PutMapping("/{userID}")
public ResponseEntity<UserEntity> updateUser(@PathVariable String userID, @RequestBody UserEntity userDetails) {
    try {
        // Lấy user hiện tại để giữ createdAt
        Optional<UserEntity> existingUser = userService.getUserByID(userID);
        if (existingUser.isPresent()) {
            userDetails.setUserID(userID);
            userDetails.setCreatedAt(existingUser.get().getCreatedAt());
        }

        UserEntity updatedUser = userService.updateUser(userDetails);
        return ResponseEntity.ok(updatedUser);
    } catch (RuntimeException e) {
        return ResponseEntity.notFound().build();
    }
}

@DeleteMapping("/{userID}")
public ResponseEntity<String> deleteUser(@PathVariable String userID) { // @PathVariable để lấy userID từ URL
    boolean deleted = userService.softDeleteUser(userID);
    if (deleted) {
        return ResponseEntity.ok("User deleted successfully");
    }
    return ResponseEntity.badRequest().body("Failed to delete user");
}

@GetMapping("/{userID}")
public ResponseEntity<UserEntity> getUser(@PathVariable String userID) {
    Optional<UserEntity> user = userService.getUserByID(userID);
    if (user.isPresent()) {
        return ResponseEntity.ok(user.get());
    }
    return ResponseEntity.notFound().build();
}

@GetMapping
public ResponseEntity<List<UserEntity>> getAllUsers() {
    List<UserEntity> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
}
}