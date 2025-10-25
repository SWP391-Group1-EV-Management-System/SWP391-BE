package charging_manage_be.services.service_package;

import charging_manage_be.model.dto.service_package.PackageTransactionResponseDTO;
import charging_manage_be.model.entity.service_package.PackageTransactionEntity;

import java.util.List;

public interface PackageTransactionService {
    boolean insertPackageTransaction(String userId, String servicePackageId, String paymentServicePackageId);

    boolean updateStatusPackageTransaction(String packageTransactionId); // Dành để sử dụng khi gói hết hạn hoặc quota = 0 thì update status --> không quan tâm đến userId

    boolean updateQuotationPackageTransaction(String packageTransactionId, String chargingSessionId);

    List<PackageTransactionResponseDTO> getPackageTransactionByUserId(String userId);

    PackageTransactionResponseDTO getLatestActivePackageByUserId(String userId);

    List<PackageTransactionResponseDTO> getPackageTransactionByUserIdAndPackageTransactionStatus(String userId, String status);

    PackageTransactionResponseDTO getPackageTransactionByPackageTransactionId(String packageTransactionId);

}
