package charging_manage_be.repository.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
   // public boolean addPayment(PaymentEntity payment);
    //public boolean existId(String id);
}