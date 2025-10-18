package charging_manage_be.model.dto.service_package;

import charging_manage_be.model.entity.service_package.PaymentServicePackageEntity;
import charging_manage_be.model.entity.service_package.ServicePackageEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageTransactionResponseDTO {
    private String packageTransactionId;
    private String userId;
    private String servicePackageId;
    private String paymentServicePackageId;
    private double remainingQuota;
    private String status;
    private LocalDateTime signPackageAt;
    private LocalDateTime expirePackageAt;
}
