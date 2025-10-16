package charging_manage_be.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "phadaoswp_jwt_secret_key_for_charging_manage_be_app_2024".getBytes(StandardCharsets.UTF_8)
    );

    // Tạo JWT từ username + role
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // thao tác getAuthority trả về 1 collection nên cần iterator để lấy phần tử đầu tiên
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        return Jwts.builder()
                .setClaims(claims) // thêm thông tin role vào payload
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // sau 1h sẽ phải login lại và tạo một token mới, token này sẽ có payload khác token cũ
                // tức là khác thời gian tạo token và hêts hạn dẫn đến chuỗi token khác nhau
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1h
                .signWith(SECRET_KEY)
                .compact();
    }

    // mã hóa tokent để lấy username từ token với SECRET_KEY
    // phục vụ cho hàm validateToken so sánh username từ token và username từ database
    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    // Kiểm tra token có hợp lệ không
    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername());
    }
}

