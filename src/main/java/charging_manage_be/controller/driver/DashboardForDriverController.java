package charging_manage_be.controller.driver;

import charging_manage_be.model.dto.driver.DashboardOfDriver;
import charging_manage_be.services.drivers.DriverDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver/dashboard")
public class DashboardForDriverController {
    @Autowired
    private DriverDashboardService driverDashboardService;

    @GetMapping("/information/{userId}")
    public ResponseEntity<DashboardOfDriver> getDashboardDriverInformation(@PathVariable String userId) {
        DashboardOfDriver dashboardOfDriver = driverDashboardService.getDashboardOfDriver(userId);
        return ResponseEntity.ok(dashboardOfDriver);
    }
}
