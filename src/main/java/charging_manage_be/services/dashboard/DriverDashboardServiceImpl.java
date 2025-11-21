package charging_manage_be.services.dashboard;

import charging_manage_be.model.dto.dashboard.DashboardOfDriver;
import charging_manage_be.model.entity.reputations.UserReputationEntity;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.payments.PaymentService;
import charging_manage_be.services.user_reputations.UserReputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DriverDashboardServiceImpl implements DriverDashboardService {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private UserReputationService userReputationService;
    @Autowired
    private ChargingSessionService chargingSessionService;

    @Override
    public DashboardOfDriver getDashboardOfDriver(String userId) {
        DashboardOfDriver dashboardOfDriver = new DashboardOfDriver();
        dashboardOfDriver.setTotalPriceIsPaid(paymentService.totalPriceIsPaid(userId));
        dashboardOfDriver.setTotalKwHBeCharged(chargingSessionService.getTotalKwhByUserId(userId));
        dashboardOfDriver.setTotalChargingSessionCompleted(chargingSessionService.countSessionsByUserIdAndIsDone(userId));
        Optional<UserReputationEntity> user = userReputationService.getCurrentUserReputationById(userId);
        if (user != null && user.isPresent()) {
            dashboardOfDriver.setReputationPoint(user.get().getCurrentScore());
        }
        return dashboardOfDriver;
    }
}
