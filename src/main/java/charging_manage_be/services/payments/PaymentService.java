package charging_manage_be.services.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.users.UserEntity;

import java.math.BigDecimal;

public interface PaymentService {
    PaymentEntity createPayment(UserEntity user, String chargingSessionId, BigDecimal price);
}
