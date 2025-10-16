package charging_manage_be.repository.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.users.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
   // public boolean addPayment(PaymentEntity payment);
    //public boolean existId(String id);
    public List<PaymentEntity> findAll();

    List<PaymentEntity> findByUser(UserEntity user);
    List<PaymentEntity> findByUserAndIsPaid(UserEntity user, boolean isPaid);
}