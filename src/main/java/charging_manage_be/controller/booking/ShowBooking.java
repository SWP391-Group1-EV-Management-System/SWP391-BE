package charging_manage_be.controller.booking;

import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.services.charging_station.ChargingStationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
@RequestMapping("/booking")
public class ShowBooking {
    private ChargingStationService chargingStationService;
    /*
     @GetMapping("/users/list")
    public ModelAndView listUsers() {
        List<UserEntity> listUsers = userService.getAllUsers();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("users", listUsers);//users phải giống biến trên html
        modelAndView.setViewName("/users/list");
        return modelAndView;
    }
    @GetMapping ("/users/add") // link trên url chrome
    public ModelAndView showAddUserForm() {
        List<RoleEntity> listRoles = roleService.findAll();
        UserEntity user = new UserEntity();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("roles", listRoles);
        modelAndView.addObject("user", user);
        modelAndView.setViewName("/users/add");//link trong folder của project
        return modelAndView;
    }
    @PostMapping("/users/add")
    public String saveUser(UserEntity user) {
        userService.addUser(user);
        return "redirect:/users/list";
    }
     */
    @GetMapping("/booking_dashboard")// link trên url chrome
    public ModelAndView showAddUserForm() {
        List<ChargingStationEntity> listStation = chargingStationService.getAllStations();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("stations", listStation);
        modelAndView.setViewName("/BookingDashboard");//link trong folder của project
        return modelAndView;
    }


}
