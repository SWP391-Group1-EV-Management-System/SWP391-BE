package charging_manage_be.model.entity.service_package;

import charging_manage_be.model.entity.payments.PaymentMethodEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.mapping.Join;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_service_package")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentServicePackageEntity  {
    @Id
    @Column(name = "payment_service_package_id", nullable = false)
    private String paymentServicePackageId;
    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    private ServicePackageEntity servicePackage;
    @ManyToOne
    @JoinColumn(name = "payment_method", nullable = false)
    private PaymentMethodEntity paymentMethod;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "is_paid", nullable = false)
    private boolean isPaid;
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @OneToOne(mappedBy = "paymentServicePackage")
    private PackageTransactionEntity packageTransaction;
}
