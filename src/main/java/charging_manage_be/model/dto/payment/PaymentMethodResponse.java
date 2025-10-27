package charging_manage_be.model.dto.payment;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodResponse {
    private String idPaymentMethod;
    private String namePaymentMethod;
}
