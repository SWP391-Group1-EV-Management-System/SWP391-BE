package charging_manage_be.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    // hash key ở đây luôn
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "phadaoswp_jwt_secret_key_for_charging_manage_be_app_2024".getBytes(StandardCharsets.UTF_8)
    );
    private final SecretKey SECRET_KEY_REFRESH = Keys.hmacShaKeyFor(
            "4202_ppa_eb_eganam_gnigrahc_rof_yek_terces_twj_pwsoadahp".getBytes(StandardCharsets.UTF_8)
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
    //Tạo refresh token để duy trì đăng nhập
    public String generateRefreshToken(UserDetails userDetails) {
        // thao tác getAuthority trả về 1 collection nên cần iterator để lấy phần tử đầu tiên
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7 day
                .signWith(SECRET_KEY_REFRESH)
                .compact();
    }
    // mã hóa tokent để lấy username từ token với SECRET_KEY
    // phục vụ cho hàm validateToken so sánh username từ token và username từ database
    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }
    public String extractUsernameRefresh(String refreshToken) {
        if (refreshToken == null) return null;
        refreshToken = refreshToken.trim();
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY_REFRESH)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody()
                    .getSubject();
        } catch (SignatureException | MalformedJwtException | IllegalArgumentException e) {
            // invalid token or bad signature
            return null;
        } catch (ExpiredJwtException e) {
            // token expired
            return null;
        }
    }

    // Kiểm tra token có hợp lệ không
    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername());
    }
}

