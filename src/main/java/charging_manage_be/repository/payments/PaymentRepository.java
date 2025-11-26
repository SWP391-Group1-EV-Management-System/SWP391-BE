package charging_manage_be.repository.payments;

import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.users.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
   // public boolean addPayment(PaymentEntity payment);
    //public boolean existId(String id);
    public List<PaymentEntity> findAll();

    List<PaymentEntity> findByUser(UserEntity user);
    List<PaymentEntity> findByUserAndIsPaid(UserEntity user, boolean isPaid);

    PaymentEntity findBySession(ChargingSessionEntity session);

    @Query("SELECT SUM(p.price) FROM PaymentEntity p WHERE p.createdAt BETWEEN :start AND :end AND p.isPaid = true")
    BigDecimal sumPriceByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByPaymentMethod_IdPaymentMethod(String paymentMethodId);

    @Query("SELECT SUM(p.price) FROM PaymentEntity p WHERE p.user = :user AND p.isPaid = :isPaid")
    BigDecimal sumPriceOfPaymentIsPaid(UserEntity user, boolean isPaid);


    List<PaymentEntity> findAllByPaymentMethod_IdPaymentMethodAndIsPaidAndSession_ChargingPost_ChargingStation_IdChargingStation(String paymentMethodIdPaymentMethod, boolean isPaid, String sessionChargingPostChargingStationIdChargingStation);
}