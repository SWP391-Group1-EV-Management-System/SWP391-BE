package charging_manage_be.model.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardAndHomeAdmin {
    private BigDecimal totalPriceToday;
    private BigDecimal totalPriceInMonth;
    private long amountUserPaidByMoney;
    private long amountUserPaidByPackage;
    private long totalActiveStations;
    private long totalActivePosts;
    private long totalSessionsInMonth;
    private long totalSessions;
    private long totalActiveUsers;
}
