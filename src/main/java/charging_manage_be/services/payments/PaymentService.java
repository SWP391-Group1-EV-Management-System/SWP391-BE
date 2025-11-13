package charging_manage_be.services.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.payments.PaymentMethodEntity;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    boolean addPayment(String sessionId, PaymentMethodEntity paymentMethod);
    boolean updatePaymentWithMethod(String paymentId,String paymentMethodId);
    PaymentEntity getPaymentByPaymentId(String paymentId);
    List<PaymentEntity> getPaymentByUserID(String userID);
    boolean invoicePayment (String paymentId);
    List<PaymentEntity> findAllPayment();
    List<PaymentEntity> findUnpaidPaymentsUser(String userId);
    List<PaymentEntity> findPaidPaymentsUser(String userId);
    PaymentEntity getPaymentBySessionId(String sessionId);

    BigDecimal totalPriceCurrentDay();
    BigDecimal totalPriceCurrentMonth();
    long totalByPaymentMethod(String paymentMethodId);
}

