package charging_manage_be.services.payments;

import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.payments.PaymentMethodEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.charging_session.ChargingSessionRepository;
import charging_manage_be.repository.payments.PaymentMethodRepository;
import charging_manage_be.repository.payments.PaymentRepository;
import charging_manage_be.repository.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
//Gắn nhãn cho class → nói với Spring “Đây là logic nghiệp vụ, quản lý tôi đi.”
//@Transactional
//Đảm bảo mọi thao tác DB trong class/method này thực hiện trọn gói, lỗi thì rollback, thành công thì commi
public class PaymentServiceImpl implements PaymentService {
    private final int characterLength = 4;
    private final int numberLength = 4;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private ChargingSessionRepository chargingSessionRepository;
    //
    //@Autowired => private Pen pen;
    // no @Autowired =>private Pen  pen = new Pen;

    private String generateUniquePaymentId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (paymentRepository.existsById(newId));
        return newId;
    }


    @Override
    public boolean addPayment(String sessionId)
    {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPaymentId(generateUniquePaymentId());

        ChargingSessionEntity session = chargingSessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        paymentEntity.setSession(session);
        paymentEntity.setUser(session.getUser());
        //paymentEntity.setPaymentMethod(paymentMethod);
        // tạo hàm riêng chọn phương thức thanh toán
        paymentEntity.setPrice(session.getTotalAmount());

        paymentRepository.save(paymentEntity);

        return true;
    }

    @Override
    public boolean processPayment(String paymentId,String paymentMethodId)
    {
        PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(paymentMethodId).orElseThrow(() -> new RuntimeException("Payment method not found"));
        PaymentEntity payment = paymentRepository.findById(paymentId).orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setPaymentMethod(paymentMethod);

        paymentRepository.save(payment);
        return true;
    }


    @Override
    public boolean updatePayment(PaymentEntity payment)
    {
        if(payment == null || paymentRepository.findById(payment.getPaymentId()).isEmpty())
        {
            return false;
        }
        paymentRepository.save(payment);
        return true;
    }

    @Override
    public PaymentEntity getPaymentByPaymentId(String paymentId) {
        if (paymentId == null || paymentRepository.findById(paymentId).isEmpty()){
            return null;
        }
        else {
            return paymentRepository.findById(paymentId).get();
        }
    }

    @Override
    public List<PaymentEntity> getPaymentByUserID(String userID) {
        if (!userRepository.existsById(userID)){
            throw  new RuntimeException("User or paymentId not found");
        }
        else{
            UserEntity user = userRepository.findById(userID).get();
            List<PaymentEntity> payments = paymentRepository.findByUser(user);
            return payments;
        }
    }
    public boolean invoicePayment(String paymentId)
    {
        PaymentEntity payment = paymentRepository.findById(paymentId).orElseThrow(() -> new RuntimeException("Payment not found"));
        if(payment == null)
        {
            return false;
        }
        payment.setPaid(true);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
        return true;
    }
    @Override
    public List<PaymentEntity> findAllPayment()
    {
        return paymentRepository.findAll();
    }

    @Override
    public List<PaymentEntity> findUnpaidPaymentsUser(String userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return paymentRepository.findByUserAndIsPaid(user, false);
    }

    @Override
    public List<PaymentEntity> findPaidPaymentsUser(String userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return paymentRepository.findByUserAndIsPaid(user, true);
    }

    /*
    public PaymentEntity createPayment(UserEntity userId, String chargingSessionId, BigDecimal price) {
        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentId(generateUniquePaymentId());
        payment.setUser(userId);
        payment.setChargingSessionId(chargingSessionId);
        payment.setPrice(price);
        // Lưu payment vào database
        if (paymentRepository.addPayment(payment)) {
            return payment;
        }
        return null;
    }
    public boolean invoicePayment(String paymentId, String paymentMethod)
    {
        PaymentEntity invoicePayment = paymentRepository.getPaymentById(paymentId);
        if(invoicePayment == null)
        {
            return false;
        }
        invoicePayment.setPaymentMethod(paymentMethod);
        invoicePayment.setPaid(true);
        return paymentRepository.updatePayment(invoicePayment);
    }
    */

}

