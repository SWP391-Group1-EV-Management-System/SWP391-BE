package charging_manage_be.services.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.payments.PaymentMethodEntity;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodService {
    boolean insertPaymentMethod(String methodName);
    boolean updatePaymentMethod(PaymentMethodEntity paymentMethod);
    boolean deletePaymentMethod(String paymentMethodId);
    Optional<PaymentMethodEntity> getPaymentMethodById(String paymentMethodId);
    List<PaymentMethodEntity> getAllPaymentMethod();
}
