package charging_manage_be.services.dashboard;


import charging_manage_be.model.dto.dashboard.DashboardOfDriver;

public interface DriverDashboardService {
    DashboardOfDriver getDashboardOfDriver(String userId);
}
