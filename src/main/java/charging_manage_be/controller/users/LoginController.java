package charging_manage_be.controller.users;

import charging_manage_be.model.dto.user.LoginRequest;
import charging_manage_be.model.dto.user.UserResponse;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.services.users.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class LoginController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> loginPost(@RequestBody LoginRequest loginRequest, HttpSession session) {
        System.out.println("Received login request for email: " + loginRequest.getEmail()); // Debug line

        UserEntity user = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        if(user == null) {
            return ResponseEntity.status(401).body("Sai email hoặc mật khẩu");
        }

        UserResponse userResponse = new UserResponse(user.getUserID(), user.getEmail(),user.getFirstName(),user.getLastName(),user.getRole());
        session.setAttribute("userSession", userResponse);
        return ResponseEntity.ok(userResponse);
    }
}