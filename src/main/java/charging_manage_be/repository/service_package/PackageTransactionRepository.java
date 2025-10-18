package charging_manage_be.repository.service_package;

import charging_manage_be.model.entity.service_package.PackageTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageTransactionRepository extends JpaRepository<PackageTransactionEntity, String> {
}
