package charging_manage_be.controller.dashboard;

import charging_manage_be.model.dto.dashboard.DashboardAndHomeAdmin;
import charging_manage_be.services.dashboard.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {
    @Autowired
    private AdminDashboardService adminDashboardService;

    @GetMapping("/information")
    public ResponseEntity<DashboardAndHomeAdmin> getDashboardInformation() {
        DashboardAndHomeAdmin dashboardAndHomeAdmin = adminDashboardService.getAdminDashboard();
        return ResponseEntity.ok(dashboardAndHomeAdmin);
    }
}
