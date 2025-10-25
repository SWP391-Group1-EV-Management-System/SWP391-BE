package charging_manage_be.services.service_package;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceAndPackageTransactionServiceImpl implements PaymentServiceAndPackageTransactionService {
    @Autowired
    private PackageTransactionService packageTransactionService;
    @Autowired
    private PaymentServicePackageService paymentServicePackageService;

    @Transactional
    @Override
    public void completePackagePurchase(String userId, String packageId, String paymentMethodId, String orderId) {
        paymentServicePackageService.insertPaymentServicePackage(packageId, userId, paymentMethodId, orderId);
        packageTransactionService.insertPackageTransaction(userId, packageId, orderId);
    }

}
