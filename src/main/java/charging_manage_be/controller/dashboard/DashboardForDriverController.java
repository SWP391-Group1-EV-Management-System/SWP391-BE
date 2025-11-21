package charging_manage_be.controller.dashboard;

import charging_manage_be.model.dto.dashboard.DashboardOfDriver;
import charging_manage_be.services.dashboard.DriverDashboardService;
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
