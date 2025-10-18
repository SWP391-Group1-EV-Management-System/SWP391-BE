package charging_manage_be.model.dto.service_package;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServicePackageResponseDTO {
    private String packageId;
    private String packageName;
    private String description;
    private int billingCycle; // month
    private BigDecimal price;
    private String unit;
    private double quota;
}
