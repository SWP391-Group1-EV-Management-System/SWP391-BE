package charging_manage_be.model.entity.service_package;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "service_package")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServicePackageEntity {
    @Id
    @Column(name = "package_id", nullable = false)
    private String packageId;
    @Column(name = "package_name", nullable = false)
    private String packageName;
    @Column(name = "description", length = 500)
    private String description;
    @Column(name = "billing_cycle", nullable = false)
    private int billingCycle; // month
    @Column(name = "price", nullable = false)
    private BigDecimal price;
    @Column(name = "unit", nullable = false)
    private String unit;
    @Column(name = "quota", nullable = false)
    private double quota;

    @OneToMany(mappedBy = "servicePackage")
    private List<PackageTransactionEntity> packageTransactions;
    @OneToMany(mappedBy = "servicePackage")
    private List<PaymentServicePackageEntity> paymentServicePackages;
}
