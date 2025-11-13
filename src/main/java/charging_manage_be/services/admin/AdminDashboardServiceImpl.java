package charging_manage_be.services.admin;

import charging_manage_be.model.dto.admin.DashboardAndHomeAdmin;
import charging_manage_be.services.charging_post.ChargingPostService;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.charging_station.ChargingStationService;
import charging_manage_be.services.payments.PaymentService;
import charging_manage_be.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Autowired
    ChargingPostService chargingPostService;
    @Autowired
    ChargingStationService chargingStationService;
    @Autowired
    PaymentService paymentService;
    @Autowired
    UserService userService;
    @Autowired
    ChargingSessionService chargingSessionService;

    @Override
    public DashboardAndHomeAdmin getAdminDashboard() {
        DashboardAndHomeAdmin dashboardAndHomeAdmin = new DashboardAndHomeAdmin();

        dashboardAndHomeAdmin.setTotalPriceToday(paymentService.totalPriceCurrentDay());
        dashboardAndHomeAdmin.setTotalPriceInMonth(paymentService.totalPriceCurrentMonth());
        dashboardAndHomeAdmin.setTotalActiveStations(chargingStationService.countTotalActiveStations());
        dashboardAndHomeAdmin.setTotalActivePosts(chargingPostService.countActivePosts());
        dashboardAndHomeAdmin.setAmountUserPaidByMoney(paymentService.totalByPaymentMethod("PMT_MOMO"));
        dashboardAndHomeAdmin.setAmountUserPaidByPackage(paymentService.totalByPaymentMethod("PMT_PACKAGE"));
        dashboardAndHomeAdmin.setTotalSessionsInMonth(chargingSessionService.countSessionsInCurrentMonth());
        dashboardAndHomeAdmin.setTotalSessions(chargingSessionService.countTotalSessions());
        dashboardAndHomeAdmin.setTotalActiveUsers(userService.countTotalActiveUsers());
        return dashboardAndHomeAdmin;
    }
}
