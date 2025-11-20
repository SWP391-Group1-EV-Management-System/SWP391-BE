package charging_manage_be.controller.dashboard;

import charging_manage_be.model.dto.dashboard.DashboardOfStaff;
import charging_manage_be.services.dashboard.StaffDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff/dashboard")
public class DashboardOfStaffController {
    @Autowired
    private StaffDashboardService staffDashboardService;

    @GetMapping("/information/{userId}")
    public ResponseEntity<DashboardOfStaff> getDashboardOfStaff(@PathVariable String userId) {
        DashboardOfStaff dashboardOfStaff = staffDashboardService.getDashboardOfStaff(userId);
        return ResponseEntity.ok(dashboardOfStaff);
    }

    @GetMapping("/all-sessions/{userId}")
    public ResponseEntity<List<DashboardOfStaff>> getAllSessionOfStation(@PathVariable String userId) {
        List<DashboardOfStaff> dashboardOfStaffList = staffDashboardService.getAllDashboardOfStaff(userId);
        return ResponseEntity.ok(dashboardOfStaffList);
    }
}
