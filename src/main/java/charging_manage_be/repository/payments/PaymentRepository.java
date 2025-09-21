package charging_manage_be.repository.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;

public interface PaymentRepository {
    public boolean addPayment(PaymentEntity payment);
    public boolean existId(String id);
}