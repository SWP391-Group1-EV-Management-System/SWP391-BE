package charging_manage_be.services.service_package;

public interface PaymentServiceAndPackageTransactionService {
    public void completePackagePurchase(String userId, String packageId, String paymentMethodId, String orderId);
}
