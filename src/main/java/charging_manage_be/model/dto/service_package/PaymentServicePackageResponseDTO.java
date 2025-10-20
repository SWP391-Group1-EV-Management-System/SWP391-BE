package charging_manage_be.model.dto.service_package;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentServicePackageResponseDTO {
    private String paymentServicePackageId;
    private String servicePackageId;
    private String paymentMethodId;
    private String userId;
    private LocalDateTime paidAt;
    private boolean isPaid;
    private BigDecimal price;
}
