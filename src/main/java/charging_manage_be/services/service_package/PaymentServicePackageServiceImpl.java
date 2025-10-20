package charging_manage_be.services.service_package;

import charging_manage_be.model.dto.service_package.PaymentServicePackageResponseDTO;
import charging_manage_be.model.entity.payments.PaymentMethodEntity;
import charging_manage_be.model.entity.service_package.PaymentServicePackageEntity;
import charging_manage_be.model.entity.service_package.ServicePackageEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.payments.PaymentMethodRepository;
import charging_manage_be.repository.service_package.PaymentServicePackageRepository;
import charging_manage_be.repository.service_package.ServicePackageRepository;
import charging_manage_be.repository.users.UserRepository;
import charging_manage_be.services.payments.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class PaymentServicePackageServiceImpl implements PaymentServicePackageService{

    @Autowired
    private PaymentServicePackageRepository paymentServicePackageRepository;
    @Autowired
    private ServicePackageRepository servicePackageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    private final int characterLength = 4;
    private final int numberLength = 4;

    private String generateUniquePaymentId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (paymentServicePackageRepository.existsById(newId));
        return newId;
    }

    @Override
    public boolean insertPaymentServicePackage(String packageId, String userId, String paymentMethodId) {
        ServicePackageEntity findPackage = servicePackageRepository.findById(packageId).orElse(null);
        UserEntity user = userRepository.findById(userId).orElse(null);
        PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(paymentMethodId).orElse(null);
        if (findPackage == null || user == null || paymentMethod == null) {
            return false;
        }
        PaymentServicePackageEntity paymentServicePackageEntity = new PaymentServicePackageEntity();
        paymentServicePackageEntity.setPaymentServicePackageId(generateUniquePaymentId());
        paymentServicePackageEntity.setServicePackage(findPackage);
        paymentServicePackageEntity.setPaymentMethod(paymentMethod);
        paymentServicePackageEntity.setUser(user);
        paymentServicePackageEntity.setPaid(false);
        paymentServicePackageEntity.setPrice(findPackage.getPrice());
        paymentServicePackageRepository.save(paymentServicePackageEntity);
        return true;

    }

    @Override
    public PaymentServicePackageEntity getPaymentServicePackageById(String paymentServicePackageId) {
        PaymentServicePackageEntity payment = paymentServicePackageRepository.findById(paymentServicePackageId).orElse(null);
        if (payment == null) {
            return null;
        }
        else{
            return payment;
        }
    }

    @Override
    public List<PaymentServicePackageEntity> getPaymentServicePackageByPackageId(String packageId) {
        ServicePackageEntity findPackage = servicePackageRepository.findById(packageId).orElse(null);
        if (findPackage == null) {
            throw new RuntimeException("Package not found");
        }
        return paymentServicePackageRepository.findByServicePackage(findPackage);

    }


    public List<PaymentServicePackageEntity> getPaymentServicePackageByUserId(String userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return paymentServicePackageRepository.findByUser(user);
    }


    public boolean invoicePaymentServicePackage(String paymentServicePackageId) {
        PaymentServicePackageEntity payment = paymentServicePackageRepository.findById(paymentServicePackageId).orElse(null);
        if (payment == null || payment.isPaid()) {
            return false;
        }
        payment.setPaid(true);
        payment.setPaidAt(LocalDateTime.now());
        paymentServicePackageRepository.save(payment);
        return true;
    }

    @Override
    public boolean processMethodPayment(String paymentServicePackageId, String paymentMethodId) {
        PaymentServicePackageEntity payment = paymentServicePackageRepository.findById(paymentServicePackageId).orElse(null);
        PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(paymentMethodId).orElse(null);
        if (payment == null || paymentMethod == null) {
            return false;
        }
        payment.setPaymentMethod(paymentMethod);
        paymentServicePackageRepository.save(payment);
        return true;
    }

    @Override
    public List<PaymentServicePackageEntity> findAllPaymentServicePackage() {
        return paymentServicePackageRepository.findAll();
    }

    @Override
    public List<PaymentServicePackageEntity> getUnpaidPaymentByUserId(String userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return paymentServicePackageRepository.findByUserAndIsPaid(user, false);
    }

    @Override
    public List<PaymentServicePackageEntity> getPaidPaymentByUserId(String userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return paymentServicePackageRepository.findByUserAndIsPaid(user, true);
    }
}
