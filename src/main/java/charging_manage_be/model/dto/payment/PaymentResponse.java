package charging_manage_be.model.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private String user;
    private String chargingSessionId;
    private boolean isPaid;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String paymentMethod;
    private BigDecimal price;
    private String session;

}

