package charging_manage_be.services.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.payments.PaymentRepository;
import charging_manage_be.repository.payments.PaymentRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
//Gắn nhãn cho class → nói với Spring “Đây là logic nghiệp vụ, quản lý tôi đi.”
//@Transactional
//Đảm bảo mọi thao tác DB trong class/method này thực hiện trọn gói, lỗi thì rollback, thành công thì commi
public class PaymentServiceImpl {
    private final int characterLength = 4;
    private final int numberLength = 4;
    @Autowired
    private PaymentRepository paymentRepository;
    //
    //@Autowired => private Pen pen;
    // no @Autowired =>private Pen  pen = new Pen;

    private String generateUniquePaymentId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (isPaymentIdExists(newId));
        return newId;
    }

    private boolean isPaymentIdExists(String id) {
        return paymentRepository.existsById(id);
    }
    public boolean addPayment(PaymentEntity payment)
    {
        if(payment == null)
        {
            return false;
        }
        payment.setPaymentId(generateUniquePaymentId());
        paymentRepository.save(payment);
        return true;
    }
    public boolean updatePayment(PaymentEntity payment)
    {
        if(payment == null || !isPaymentIdExists(payment.getPaymentId()))
        {
            return false;
        }
        paymentRepository.save(payment);
        return true;
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

