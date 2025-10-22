package charging_manage_be.model.dto.service_package;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentServicePackageCreate {
    String userId;
    String packageId;
}
