package charging_manage_be.model.dto.service_package;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServicePackageRequestDTO {
    private String packageId;
    private String packageName;
    private String description;
    private int billingCycle; // month
    private BigDecimal price;
    private String unit;
    private double quota;
}
