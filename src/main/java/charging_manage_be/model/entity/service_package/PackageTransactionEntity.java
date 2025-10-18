package charging_manage_be.model.entity.service_package;

import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "package_transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageTransactionEntity {
    @Id
    @Column(name = "package_transaction_id", nullable = false)
    private String packageTransactionId;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @ManyToOne
    @JoinColumn(name = "service_package_id", nullable = false)
    private ServicePackageEntity servicePackage;
    @OneToOne
    @JoinColumn(name = "payment_service_package_id", nullable = false)
    private PaymentServicePackageEntity paymentServicePackage;
    @Column(name = "remaining_quota", nullable = false)
    private double remainingQuota;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "sign_package_at", nullable = false)
    private LocalDateTime signPackageAt;
    @Column(name = "expire_package_at", nullable = false)
    private LocalDateTime expirePackageAt;





}
