package charging_manage_be.model.dto.service_package;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentServicePackageRequestDTO {
    private String paymentServicePackageId;
    private String servicePackageId;
    private String paymentMethod;
    private String userId;
    private LocalDateTime paidAt;
    private boolean isPaid;
    private BigDecimal price;
}
