package charging_manage_be.services.payments;

import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.payments.PaymentMethodEntity;
import charging_manage_be.repository.charging_session.ChargingSessionRepository;
import charging_manage_be.repository.payments.PaymentMethodRepository;
import charging_manage_be.repository.payments.PaymentRepository;
import charging_manage_be.repository.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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
    public boolean addPayment(String sessionId, String paymentMethodId, BigDecimal price)
    {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPaymentId(generateUniquePaymentId());

        ChargingSessionEntity session = chargingSessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(paymentMethodId).orElseThrow(() -> new RuntimeException("Payment method not found"));

        paymentEntity.setSession(session);
        paymentEntity.setUser(session.getUser());
        paymentEntity.setChargingSessionId(sessionId);
        paymentEntity.setPaymentMethod(paymentMethod);
        paymentEntity.setPrice(price);

        paymentRepository.save(paymentEntity);

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
    public PaymentEntity getPaymentByUserID(String userID, String paymentId) {
        if (!userRepository.existsById(userID) || paymentRepository.findById(paymentId).isEmpty()){
            throw  new RuntimeException("User or paymentId not found");
        }
        else{
            PaymentEntity payment = paymentRepository.findById(paymentId).get();
            if(payment.getUser().getUserID().equals(userID))
            {
                return payment;
            }
            else
            {
                throw  new RuntimeException("Payment does not belong to user");
            }
        }
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

