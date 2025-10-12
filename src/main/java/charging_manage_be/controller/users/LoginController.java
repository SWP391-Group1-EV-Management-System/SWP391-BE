package charging_manage_be.controller.users;

import charging_manage_be.model.dto.user.LoginRequest;
import charging_manage_be.model.dto.user.UserResponse;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.services.status_service.UserStatusService;
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
    @Autowired
    private UserStatusService userStatusService;

    @PostMapping("/login")
    public ResponseEntity<?> loginPost(@RequestBody LoginRequest loginRequest, HttpSession session) {
        System.out.println("Received login request for email: " + loginRequest.getEmail()); // Debug line

        UserEntity user = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        if(user == null) {
            return ResponseEntity.status(401).body("Sai email hoặc mật khẩu");
        }
        if(!user.isStatus()) {
            return ResponseEntity.status(401).body("Tài Khoản Đã Bị Chặn");
        }
        UserResponse userResponse = new UserResponse(user.getUserID(), user.getEmail(),user.getFirstName(),user.getLastName(),user.getRole());
        session.setAttribute("userSession", userResponse);
        String userStatus = userStatusService.getUserStatus(user.getUserID());
        if(userStatus == null ) {
            userStatusService.idleUserStatus(user.getUserID());
        }
        session.setAttribute("action", userStatus);
        return ResponseEntity.ok(userResponse);
    }
}
