package charging_manage_be.services.UserToken;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;
@Service
public class CustomerDetailService implements UserDetailsService {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // lưu chuỗi password được mã hóa lên để so sánh
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        // Trả về đối tượng userdetails builder (của Spring)
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()) // định danh user
                .password(encodedPassword)
                .roles(user.getRole())
                .build();
    }
}
