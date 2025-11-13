package charging_manage_be.controller.admin;

import charging_manage_be.model.dto.admin.DashboardAndHomeAdmin;
import charging_manage_be.services.admin.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
