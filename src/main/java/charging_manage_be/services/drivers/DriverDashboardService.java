package charging_manage_be.services.drivers;


import charging_manage_be.model.dto.driver.DashboardOfDriver;

public interface DriverDashboardService {
    DashboardOfDriver getDashboardOfDriver(String userId);
}
