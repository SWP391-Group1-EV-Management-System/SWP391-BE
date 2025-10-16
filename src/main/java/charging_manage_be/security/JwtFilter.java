package charging_manage_be.security;

import charging_manage_be.services.UserToken.CustomerDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerDetailService detailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        //final String authHeader = request.getHeader("Authorization"); // Lấy header sau khi đăng nhập thành công
        String email = null;
        String jwt = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("jwt")) {
                    jwt = cookie.getValue();  // Lấy token thuần từ cookie
                    break;
                }
            }
        }

        if (jwt != null) {
            email = jwtUtil.extractUsername(jwt);
        }
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) { // kiểm tra email và request trống
            //email hợp lệ tiếp tục kiểm tra
            UserDetails userDetails = detailsService.loadUserByUsername(email);

            if (jwtUtil.validateToken(jwt, userDetails)) { // Kiểm tra token có đúng username không và chưa hết hạn
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                /*
                Sau dòng này, các API yêu cầu quyền ( @PreAuthorize("hasRole('ADMIN')"))
                sẽ hoạt động đúng, vì Spring biết bạn là ai và có role gì.
                 */
            }
        }
        chain.doFilter(request, response); // luôn luôn cho phép đi tiếp nếu không có quyền sẽ bị chặn lại bởi ( @PreAuthorize("hasRole('ADMIN')"))
    }
}
