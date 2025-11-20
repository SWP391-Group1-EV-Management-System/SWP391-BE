package charging_manage_be.services.dashboard;

import charging_manage_be.model.dto.dashboard.DashboardOfStaff;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.charging_station.ChargingStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class StaffDashboardServiceImpl implements StaffDashboardService {
    @Autowired
    private ChargingSessionService chargingSessionService;
    @Autowired
    private ChargingStationService chargingStationService;

    @Override
    public DashboardOfStaff getDashboardOfStaff(String userId) {
        DashboardOfStaff dashboardOfStaff = new DashboardOfStaff();
        dashboardOfStaff.setTotalSessionInStation(chargingSessionService.countSessionsByStation(userId));
        dashboardOfStaff.setTotalSessionIsProcessingInStation(chargingSessionService.countSessionIsProgressByStation(userId));
        dashboardOfStaff.setTotalSessionCompletedInStation(chargingSessionService.countSessionIsDoneByStation(userId));
        dashboardOfStaff.setTotalRevenueInStation(chargingSessionService.getRevenueByStation(userId));
        return dashboardOfStaff;
    }

    @Override
    public List<DashboardOfStaff> getAllDashboardOfStaff(String userId) {

        ChargingStationEntity station = chargingStationService.getStationByUserId(userId);
        String stationId = station.getIdChargingStation();
        List<ChargingSessionEntity> chargingSessionEntityList = chargingSessionService.getAllSessionsByStationId(stationId);
        List<DashboardOfStaff> dashboardOfStaffList = new ArrayList<>();

        for (ChargingSessionEntity chargingSessionEntity : chargingSessionEntityList) {
            DashboardOfStaff staff = new DashboardOfStaff();

            staff.setChargingSessionId(chargingSessionEntity.getChargingSessionId());
            staff.setUserName(chargingSessionEntity.getUser().getFirstName() + " " + chargingSessionEntity.getUser().getLastName());
            staff.setPostName(chargingSessionEntity.getChargingPost().getIdChargingPost());
            staff.setDateCreated(chargingSessionEntity.getStartTime());
            staff.setStatus(chargingSessionEntity.isDone());
            staff.setStartedTime(chargingSessionEntity.getStartTime());
            staff.setEndedTime(chargingSessionEntity.getEndTime());
            staff.setKWh(chargingSessionEntity.getKWh());
            staff.setTotalAmount(chargingSessionEntity.getTotalAmount());

            dashboardOfStaffList.add(staff);
        }
        return dashboardOfStaffList;
    }
}
