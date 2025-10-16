package charging_manage_be.controller.users;

import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.services.email.EmailService;
import charging_manage_be.services.users.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/users/register")
public class RegisterController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;


//    @PostMapping
//    ResponseEntity<String> registerUser(@RequestBody UserEntity userEntity) {
//        try {
//            // Đăng ký người dùng mới
//            UserEntity existingUser = userService.registerUser(userEntity);
//
//            // Gửi OTP vào email của người dùng
//            emailService.sendOtpEmail(existingUser.getEmail());
//            return ResponseEntity.ok("User registered successfully. OTP has been sent to your email.");
//        } catch (IllegalArgumentException ex) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
//        } catch (MessagingException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP email: " + e.getMessage());
//        }
//    }

    @PostMapping
    ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) throws ParseException {
        String email = request.get("email");
        // Kiểm tra nếu email đã tồn tại
        if (userService.findByEmail(email).isPresent()) { // Nếu email đã tồn tại
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is already registered.");
        } else {
            try {
                UserEntity newUser = new UserEntity();
                newUser.setEmail(email);
                newUser.setFirstName(request.get("firstName"));
                newUser.setLastName(request.get("lastName"));
                newUser.setBirthDate(new SimpleDateFormat("yyyy-MM-dd").parse(request.get("birthDate")));
                newUser.setGender(Boolean.parseBoolean(request.get("gender")));
                newUser.setPhoneNumber(request.get("phoneNumber"));
                newUser.setPassword(request.get("password"));

                userService.saveUserTemp(newUser);

                try {
                    // Gửi OTP vào email của người dùng
                    emailService.sendOtpEmail(newUser.getEmail());
                    return ResponseEntity.ok("OTP has been sent to your email. Please verify to complete registration.");
                }
                catch (IllegalArgumentException ex) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
                }
                catch (MessagingException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP email: " + e.getMessage());
                }
            }
            catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process registration: " + ex.getMessage());
            }
        }
    }


    // Endpoint xác thực OTP
    @PostMapping("/verify-otp")
    ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otpCode = request.get("otp");

            if (email == null || email.isEmpty() || otpCode == null || otpCode.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and OTP are required.");
            }

            // Kiểm tra mã OTP
            boolean isValidOtp = emailService.verifyOtp(email, otpCode);
            if (!isValidOtp) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP code.");
            }
            else {
                UserEntity newUser = userService.getTempUserByEmail(email);
                userService.registerUser(newUser);
                return ResponseEntity.ok("OTP has been verified and user registered successfully.");
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to verify OTP: " + ex.getMessage());
        }
    }


    @PostMapping("/resend-otp")
    ResponseEntity<String> resendOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is required");
            }

            // Kiểm tra xem user tạm thời có tồn tại trong Redis không
            if (userService.getTempUserByEmail(email) == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found or registration expired. Please register again.");
            }

            // Gửi lại OTP mới
            emailService.sendOtpEmail(email);
            return ResponseEntity.ok("New OTP has been sent to your email.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to resend OTP: " + ex.getMessage());
        }
    }


}
