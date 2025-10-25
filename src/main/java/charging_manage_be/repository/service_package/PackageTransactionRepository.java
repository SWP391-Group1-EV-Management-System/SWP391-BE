package charging_manage_be.repository.service_package;

import charging_manage_be.model.entity.service_package.PackageTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageTransactionRepository extends JpaRepository<PackageTransactionEntity, String> {
    // Làm get PackageTransactionEntity By User, By Status
    List<PackageTransactionEntity> findByUser_UserID(String userId);
    List<PackageTransactionEntity> findByStatus(String status);
    List<PackageTransactionEntity> findByUser_UserIDAndStatus(String userId, String status);
    PackageTransactionEntity findFirstByUser_UserIDAndStatusOrderBySignPackageAtDesc(String userId, String status);


}
