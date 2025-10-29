package charging_manage_be.model.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDetail {
    private String paymentId;
    private String sessionId;
    private boolean isPaid;
    private String chargingStationName;
    private BigDecimal kWh;
    private BigDecimal price;
}
