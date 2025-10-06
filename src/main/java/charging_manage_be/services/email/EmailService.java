package charging_manage_be.services.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private int characterLength = 0;
    private int numberLength = 6;

    public String generateUniqueId() {
        String newId;
            newId = generateRandomId(characterLength, numberLength);
        return newId;
    }

    public void sendOtpEmail(String toEmail) throws MessagingException {
        String otp = generateUniqueId();

        //Create MimeMessage có nghĩa là tạo một email tới người nhận
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true); // lớp helper giúp thiết lập các thuộc tính của email

        // Set thông tin email
        helper.setFrom("tramsacecoz@gmail.com");
        helper.setTo(toEmail);
        helper.setSubject("Your OTP Code");
        helper.setText("Your OTP is: " + otp);

        // Gửi email
        mailSender.send(message);
        System.out.println("OTP email sent to " + toEmail + " with OTP: " + otp);

        // Lưu OTP vào Redis với thời gian hết hạn là 5 phút (300 giây)
        redisTemplate.opsForValue().set("otp:" + toEmail, otp, 1, TimeUnit.MINUTES);

    }


    public boolean verifyOtp(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get("otp:" + email);
        // Sẽ lấy OTP từ redis dựa theo email và trả ra value là OTP

        // Sẽ có 2 trường hợp xảy ra:
        // 1. OTP đúng và chưa hết hạn: storedOtp sẽ khác null và bằng otp
        // 2. OTP sai hoặc đã hết hạn: storedOtp sẽ là null hoặc khác otp
        // Chỉ xóa OTP khỏi Redis khi xác thực thành công hoặc hết hạn thì phải dùng
        if (storedOtp != null && storedOtp.equals(otp)) {
            // Xóa OTP khỏi Redis sau khi xác thực thành công
            redisTemplate.delete(email);
            return true;

        }
        return false;
    }
}
