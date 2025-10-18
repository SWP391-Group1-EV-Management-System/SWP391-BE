package charging_manage_be.services.service_package;

import charging_manage_be.model.dto.service_package.PaymentServicePackageResponseDTO;
import charging_manage_be.model.entity.service_package.PaymentServicePackageEntity;

import java.util.List;

public interface PaymentServicePackageService {
    boolean insertPaymentServicePackage(String packageId, String userId, String paymentMethodId);
    PaymentServicePackageEntity getPaymentServicePackageById(String paymentServicePackageId);
    List<PaymentServicePackageEntity> getPaymentServicePackageByPackageId(String packageId);
    List<PaymentServicePackageEntity> getPaymentServicePackageByUserId(String userId);
    boolean invoicePaymentServicePackage(String paymentServicePackageId);
    boolean processMethodPayment(String paymentServicePackageId, String paymentMethodId);
    List<PaymentServicePackageEntity> findAllPaymentServicePackage();
    List<PaymentServicePackageEntity> getUnpaidPaymentByUserId(String userId);
    List<PaymentServicePackageEntity> getPaidPaymentByUserId(String userId);
}
