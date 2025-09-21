package charging_manage_be.repository.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @PersistenceContext
    private EntityManager entityManager;
    // boolean exists = entityManager.find(PaymentEntity.class, id) != null;
    public boolean existId(String paymentId) {
        return entityManager.find(PaymentEntity.class, paymentId) != null;
    }
    @Transactional
    public boolean addPayment(PaymentEntity payment) {
        try {
            entityManager.persist(payment);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Transactional
    public boolean updatePayment(PaymentEntity payment) {
        try {
            entityManager.merge(payment);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public PaymentEntity getPaymentById(String paymentId) {
        return entityManager.find(PaymentEntity.class, paymentId);
    }


}
