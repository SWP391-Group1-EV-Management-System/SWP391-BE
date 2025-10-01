package charging_manage_be.controller.users;

import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.services.users.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    @Autowired
    private UserService userService;
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @PostMapping("/login")
    public String loginPost(HttpSession session, @RequestParam(value="email") String email, @RequestParam(value="password") String password) {
        UserEntity user = userService.loginUser(email, password);
        // sai thì về login
        if(user == null) {
            return "redirect:/login?error";
        }
        // đúng thì set session
        session.setAttribute("user", user);
        //đúng thì về home nhưng phải set controller cho home
        return "redirect:/booking_dashboard";
    }
    @GetMapping ("/403")
    public String accessDenied() {
        return "403";
    }
}
