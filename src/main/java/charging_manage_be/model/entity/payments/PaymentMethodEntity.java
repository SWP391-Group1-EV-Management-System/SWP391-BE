package charging_manage_be.model.entity.payments;

import charging_manage_be.model.entity.reputations.UserReputationEntity;
import charging_manage_be.model.entity.service_package.PackageTransactionEntity;
import charging_manage_be.model.entity.service_package.PaymentServicePackageEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "payment_method")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodEntity {
    @Id
    @Column(name = "id_payment_method", nullable = false)
    private String idPaymentMethod;
    @Column(name = "name_payment_method", nullable = false)
    private String namePaymentMethod;

    @OneToMany(mappedBy = "paymentMethod")
    private List<PaymentEntity> paymentEntities;
    @OneToMany(mappedBy = "paymentMethod")
    private List<PaymentServicePackageEntity> paymentServicePackages;
}
