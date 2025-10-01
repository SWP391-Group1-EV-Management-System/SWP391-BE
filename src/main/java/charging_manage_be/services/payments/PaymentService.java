package charging_manage_be.services.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.payments.PaymentMethodEntity;

import java.math.BigDecimal;

public interface PaymentService {
    boolean addPayment(String sessionId, String paymentMethodId, BigDecimal price);
    boolean updatePayment(PaymentEntity payment);
    PaymentEntity getPaymentByPaymentId(String paymentId);
    PaymentEntity getPaymentByUserID(String userID, String paymentId);
}
