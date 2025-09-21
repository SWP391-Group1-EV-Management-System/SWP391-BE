package charging_manage_be.services.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.payments.PaymentRepositoryImpl;
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
    private PaymentRepositoryImpl paymentRepositoryImpl;
    //@Autowired
    //@Autowired => private Pen pen;
    // no @Autowired =>private Pen  pen = new Pen;
    public PaymentServiceImpl(PaymentRepositoryImpl RepositoryImpl)
    {
        this.paymentRepositoryImpl =  RepositoryImpl;
    }

    private String generateUniquePaymentId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (isPaymentIdExists(newId));
        return newId;
    }

    private boolean isPaymentIdExists(String id) {
        return paymentRepositoryImpl.existId(id);
    }

    public PaymentEntity createPayment(UserEntity userId, String chargingSessionId, BigDecimal price) {
        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentId(generateUniquePaymentId());
        payment.setUser(userId);
        payment.setChargingSessionId(chargingSessionId);
        payment.setPrice(price);
        // Lưu payment vào database
        if (paymentRepositoryImpl.addPayment(payment)) {
            return payment;
        }
        return null;
    }
    public boolean invoicePayment(String paymentId, String paymentMethod)
    {
        PaymentEntity invoicePayment = paymentRepositoryImpl.getPaymentById(paymentId);
        if(invoicePayment == null)
        {
            return false;
        }
        invoicePayment.setPaymentMethod(paymentMethod);
        invoicePayment.setPaid(true);
        return paymentRepositoryImpl.updatePayment(invoicePayment);
    }

}

