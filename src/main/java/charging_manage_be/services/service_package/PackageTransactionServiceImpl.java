package charging_manage_be.services.service_package;

import charging_manage_be.model.dto.service_package.PackageTransactionRequestDTO;
import charging_manage_be.model.dto.service_package.PackageTransactionResponseDTO;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.service_package.PackageTransactionEntity;
import charging_manage_be.model.entity.service_package.PaymentServicePackageEntity;
import charging_manage_be.model.entity.service_package.ServicePackageEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.charging_session.ChargingSessionRepository;
import charging_manage_be.repository.service_package.PackageTransactionRepository;
import charging_manage_be.repository.service_package.PaymentServicePackageRepository;
import charging_manage_be.repository.service_package.ServicePackageRepository;
import charging_manage_be.repository.users.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class PackageTransactionServiceImpl implements PackageTransactionService {

    @Autowired
    private PackageTransactionRepository packageTransactionRepository;



    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaymentServicePackageRepository paymentServicePackageRepository;
    @Autowired
    private ServicePackageRepository servicePackageRepository;
    @Autowired
    private ChargingSessionRepository chargingSessionRepository;

    private final int characterLength = 4;
    private final int numberLength = 4;

    public String generateUniquePaymentId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (packageTransactionRepository.existsById(newId));
        return newId;
    }


    @Override
    public boolean insertPackageTransaction(String userId, String servicePackageId, String paymentServicePackageId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        ServicePackageEntity servicePackage = servicePackageRepository.findById(servicePackageId).orElse(null);
        PaymentServicePackageEntity paymentServicePackage = paymentServicePackageRepository.findById(paymentServicePackageId).orElse(null);

        if (user == null || servicePackage == null || paymentServicePackage == null) {
            return false;
        }

        PackageTransactionEntity packageTransaction = new PackageTransactionEntity();
        packageTransaction.setPackageTransactionId(generateUniquePaymentId());
        packageTransaction.setSignPackageAt(paymentServicePackage.getPaidAt());
        packageTransaction.setExpirePackageAt(paymentServicePackage.getPaidAt().plusMonths(servicePackage.getBillingCycle()));
        packageTransaction.setRemainingQuota(BigDecimal.valueOf(servicePackage.getQuota()));
        packageTransaction.setStatus("ACTIVE");
        packageTransaction.setPaymentServicePackage(paymentServicePackage);
        packageTransaction.setServicePackage(servicePackage);
        packageTransaction.setUser(user);

        packageTransactionRepository.save(packageTransaction);
        return true;
    }

    @Override
    public boolean updateStatusPackageTransaction(String packageTransactionId) {
        PackageTransactionEntity packageTransaction = packageTransactionRepository.findById(packageTransactionId).orElse(null);
        if (packageTransaction == null) {
            return false;
        }
        packageTransaction.setStatus("INACTIVE");
        packageTransactionRepository.save(packageTransaction);
        return true;
    }

    @Override
    @Transactional
    public boolean updateQuotationPackageTransaction(String packageTransactionId, String chargingSessionId) { // Thằng này phải dùng ở việc sau khi thanh toán mới update chứ không phải dùng trong controller transaction
        PackageTransactionEntity packageTransaction = packageTransactionRepository.findById(packageTransactionId).orElse(null);
        ChargingSessionEntity session = chargingSessionRepository.findById(chargingSessionId).orElse(null);

        if (packageTransaction == null || session == null) {
            return false;
        }
        if (session.getKWh() == null || session.getKWh().compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("Invalid session: kWh must be greater than zero");
        }

        if (!"ACTIVE".equals(packageTransaction.getStatus())){
            throw new RuntimeException("Package Transaction is not ACTIVE");
        }

        BigDecimal usedKWh = session.getKWh();
        BigDecimal remainingQuota = packageTransaction.getRemainingQuota();

        if (usedKWh.compareTo(remainingQuota) >= 0){ // Dòng này là nếu kWh sử dụng lớn hơn hoặc bằng quota còn lại
            packageTransaction.setRemainingQuota(BigDecimal.valueOf(0));
            packageTransaction.setStatus("INACTIVE");
        }
        else if (usedKWh.compareTo(remainingQuota) < 0){
            packageTransaction.setRemainingQuota(remainingQuota.subtract(usedKWh));
        }

        packageTransactionRepository.save(packageTransaction);
        return true;
    }

    @Override
    public boolean deletePackageTransactionById(String packageTransactionId) {
        PackageTransactionEntity packageTransaction = packageTransactionRepository.findById(packageTransactionId).orElse(null);
        if (packageTransaction == null) {
            throw new RuntimeException("Service package transaction with id " + packageTransactionId + " not found");
        }
        packageTransactionRepository.deleteById(packageTransactionId);
        return true;
    }

    @Override
    public List<PackageTransactionResponseDTO> getPackageTransactionByUserId(String userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        List<PackageTransactionEntity> packageTransactionEntityList = packageTransactionRepository.findByUser_UserID(userId);
        if (packageTransactionEntityList.isEmpty()) {
            throw new RuntimeException("No package transaction found");
        }
        List<PackageTransactionResponseDTO> packageTransactionResponseDTOList = new ArrayList<>();
        for (PackageTransactionEntity packageTransactionEntity : packageTransactionEntityList) {
            PackageTransactionResponseDTO packageTransactionResponseDTO = new PackageTransactionResponseDTO();

            packageTransactionResponseDTO.setPackageTransactionId(packageTransactionEntity.getPackageTransactionId());
            packageTransactionResponseDTO.setRemainingQuota(packageTransactionEntity.getRemainingQuota());
            packageTransactionResponseDTO.setSignPackageAt(packageTransactionEntity.getSignPackageAt());
            packageTransactionResponseDTO.setExpirePackageAt(packageTransactionEntity.getExpirePackageAt());
            packageTransactionResponseDTO.setStatus(packageTransactionEntity.getStatus());
            packageTransactionResponseDTO.setPaymentServicePackageId(packageTransactionEntity.getPaymentServicePackage().getPaymentServicePackageId());
            packageTransactionResponseDTO.setServicePackageId(packageTransactionEntity.getServicePackage().getPackageId());
            packageTransactionResponseDTO.setUserId(userId);

            packageTransactionResponseDTOList.add(packageTransactionResponseDTO);
            // Hoặc nếu không muốn dài loằng ngoằng như trên thì dùng packageTransactionResponseDTOList.add(mapToDTO(packageTransactionEntity));
        }
        return packageTransactionResponseDTOList;
    }

    @Override
    public PackageTransactionResponseDTO getLatestActivePackageByUserId(String userId) {
        PackageTransactionEntity transactions = packageTransactionRepository.findFirstByUser_UserIDAndStatusOrderBySignPackageAtDesc(userId, "ACTIVE");

        if (transactions == null) {
            throw new RuntimeException("No active package found");
        }

        PackageTransactionResponseDTO packageTransactionResponseDTO = new PackageTransactionResponseDTO();
        packageTransactionResponseDTO.setPackageTransactionId(transactions.getPackageTransactionId());
        packageTransactionResponseDTO.setRemainingQuota(transactions.getRemainingQuota());
        packageTransactionResponseDTO.setSignPackageAt(transactions.getSignPackageAt());
        packageTransactionResponseDTO.setExpirePackageAt(transactions.getExpirePackageAt());
        packageTransactionResponseDTO.setStatus(transactions.getStatus());
        packageTransactionResponseDTO.setPaymentServicePackageId(transactions.getPaymentServicePackage().getPaymentServicePackageId());
        packageTransactionResponseDTO.setServicePackageId(transactions.getServicePackage().getPackageId());
        packageTransactionResponseDTO.setUserId(userId);
        return packageTransactionResponseDTO;
    }

    @Override
    public List<PackageTransactionResponseDTO> getPackageTransactionByUserIdAndPackageTransactionStatus(String userId, String status) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        List<PackageTransactionEntity> packageTransactionEntityList = packageTransactionRepository.findByUser_UserIDAndStatus(user.getUserID(), status.toUpperCase());
        if (packageTransactionEntityList.isEmpty()) {
            throw new RuntimeException("No package transaction found");
        }
        List<PackageTransactionResponseDTO> packageTransactionResponseDTOList = new ArrayList<>();
        for (PackageTransactionEntity packageTransactionEntity : packageTransactionEntityList) {
            PackageTransactionResponseDTO packageTransactionResponseDTO = new PackageTransactionResponseDTO();

            packageTransactionResponseDTO.setPackageTransactionId(packageTransactionEntity.getPackageTransactionId());
            packageTransactionResponseDTO.setRemainingQuota(packageTransactionEntity.getRemainingQuota());
            packageTransactionResponseDTO.setSignPackageAt(packageTransactionEntity.getSignPackageAt());
            packageTransactionResponseDTO.setExpirePackageAt(packageTransactionEntity.getExpirePackageAt());
            packageTransactionResponseDTO.setStatus(status);
            packageTransactionResponseDTO.setPaymentServicePackageId(packageTransactionEntity.getPaymentServicePackage().getPaymentServicePackageId());
            packageTransactionResponseDTO.setServicePackageId(packageTransactionEntity.getServicePackage().getPackageId());
            packageTransactionResponseDTO.setUserId(userId);

            packageTransactionResponseDTOList.add(packageTransactionResponseDTO);
        }
        return packageTransactionResponseDTOList;
    }


    @Override
    public PackageTransactionResponseDTO getPackageTransactionByPackageTransactionId(String packageTransactionId) {
        PackageTransactionEntity packageTransactionEntity = packageTransactionRepository.findById(packageTransactionId).orElse(null);
        if (packageTransactionEntity == null) {
            throw new RuntimeException("No package transaction found");
        }
        else{
            PackageTransactionResponseDTO packageTransactionResponseDTO = new PackageTransactionResponseDTO();
            packageTransactionResponseDTO.setPackageTransactionId(packageTransactionEntity.getPackageTransactionId());
            packageTransactionResponseDTO.setRemainingQuota(packageTransactionEntity.getRemainingQuota());
            packageTransactionResponseDTO.setSignPackageAt(packageTransactionEntity.getSignPackageAt());
            packageTransactionResponseDTO.setExpirePackageAt(packageTransactionEntity.getExpirePackageAt());
            packageTransactionResponseDTO.setStatus(packageTransactionEntity.getStatus());
            packageTransactionResponseDTO.setPaymentServicePackageId(packageTransactionEntity.getPaymentServicePackage().getPaymentServicePackageId());
            packageTransactionResponseDTO.setServicePackageId(packageTransactionEntity.getServicePackage().getPackageId());
            packageTransactionResponseDTO.setUserId(packageTransactionEntity.getUser().getUserID());
            return packageTransactionResponseDTO;
        }
    }
}
