package charging_manage_be.repository.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class PaymentRepositoryImpl implements PaymentRepository {
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    public PaymentRepositoryImpl(String jpaName) {
        this.entityManagerFactory = Persistence.createEntityManagerFactory(jpaName);// lấy cấu hình từ persistence-unit name="JpaName" có tên là  gì tùy chọn
        this.entityManager = entityManagerFactory.createEntityManager();
    }
    // boolean exists = entityManager.find(PaymentEntity.class, id) != null;
    public boolean existId(String paymentId) {
        return entityManager.find(PaymentEntity.class, paymentId) != null;
    }
    public boolean addPayment(PaymentEntity payment) {
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(payment);
            entityManager.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    public boolean updatePayment(PaymentEntity payment) {
        try {
            entityManager.getTransaction().begin();
            entityManager.merge(payment);
            entityManager.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    public PaymentEntity getPaymentById(String paymentId) {
        return entityManager.find(PaymentEntity.class, paymentId);
    }


}
