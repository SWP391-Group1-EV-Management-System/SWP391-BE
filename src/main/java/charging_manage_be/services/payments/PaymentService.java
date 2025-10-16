package charging_manage_be.services.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.payments.PaymentMethodEntity;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    boolean addPayment(String sessionId);
    boolean updatePayment(PaymentEntity payment);
    PaymentEntity getPaymentByPaymentId(String paymentId);
    List<PaymentEntity> getPaymentByUserID(String userID);
    boolean processPayment(String paymentId,String paymentMethodId);
    boolean invoicePayment (String paymentId);
    List<PaymentEntity> findAllPayment();
    List<PaymentEntity> findUnpaidPaymentsUser(String userId);
    List<PaymentEntity> findPaidPaymentsUser(String userId);

}

