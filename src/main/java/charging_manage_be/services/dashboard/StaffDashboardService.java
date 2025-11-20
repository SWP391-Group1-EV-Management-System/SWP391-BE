package charging_manage_be.services.dashboard;

import charging_manage_be.model.dto.dashboard.DashboardOfStaff;

import java.util.List;

public interface StaffDashboardService {
    DashboardOfStaff getDashboardOfStaff(String stationId);

    List<DashboardOfStaff> getAllDashboardOfStaff(String stationId);
}
