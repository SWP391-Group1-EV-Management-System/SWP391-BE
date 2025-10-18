package charging_manage_be.repository.service_package;

import charging_manage_be.model.dto.service_package.PaymentServicePackageResponseDTO;
import charging_manage_be.model.entity.service_package.PaymentServicePackageEntity;
import charging_manage_be.model.entity.service_package.ServicePackageEntity;
import charging_manage_be.model.entity.users.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentServicePackageRepository extends JpaRepository<PaymentServicePackageEntity, String> {
    List<PaymentServicePackageEntity> findByUser(UserEntity user);
    List<PaymentServicePackageEntity> findByServicePackage(ServicePackageEntity servicePackage);
    List<PaymentServicePackageEntity> findByUserAndIsPaid(UserEntity user, boolean isPaid);
}
