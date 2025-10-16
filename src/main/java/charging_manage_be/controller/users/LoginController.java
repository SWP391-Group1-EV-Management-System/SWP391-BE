package charging_manage_be.controller.users;

import charging_manage_be.model.dto.user.LoginRequest;
import charging_manage_be.model.dto.user.UserResponse;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.security.JwtUtil;
import charging_manage_be.services.UserToken.CustomerDetailService;
import charging_manage_be.services.status_service.UserStatusService;
import charging_manage_be.services.users.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RequestMapping("/users")
@RestController
public class LoginController {
    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private CustomerDetailService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tài khoản hoặc mật khẩu");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true) // không cho JS đọc tránh XXS
                .secure(true) // chỉ gửi qua HTTPS
                .path("/")
                .maxAge(24 * 60 * 60) // 1 ngày
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Đăng nhập thành công!");
    }

    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Chào ADMIN 👑");
    }
}



//@Autowired
//private UserService userService;
//@Autowired
//private UserStatusService userStatusService;
// @PostMapping("/login")
//    public ResponseEntity<?> loginPost(@RequestBody LoginRequest loginRequest, HttpSession session) {
//        System.out.println("Received login request for email: " + loginRequest.getEmail()); // Debug line
//
//        UserEntity user = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
//        if(user == null) {
//            return ResponseEntity.status(401).body("Sai email hoặc mật khẩu");
//        }
//        if(!user.isStatus()) {
//            return ResponseEntity.status(401).body("Tài Khoản Đã Bị Chặn");
//        }
//        UserResponse userResponse = new UserResponse(user.getUserID(), user.getEmail(),user.getFirstName(),user.getLastName(),user.getRole(), user.getCreatedAt());
//        session.setAttribute("userSession", userResponse);
//        String userStatus = userStatusService.getUserStatus(user.getUserID());
//        if(userStatus == null ) {
//            userStatusService.idleUserStatus(user.getUserID());
//        }
//        session.setAttribute("action", userStatus);
//        return ResponseEntity.ok(userResponse);
//    }