package charging_manage_be.model.dto.driver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardOfDriver {
    private BigDecimal totalPriceIsPaid;
    private BigDecimal totalKwHBeCharged;
    private int totalChargingSessionCompleted;
    private int reputationPoint;
}
