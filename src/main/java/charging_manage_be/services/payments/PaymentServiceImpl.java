package charging_manage_be.services.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.repository.payments.PaymentRepositoryImpl;

import java.math.BigDecimal;

import static charging_manage_be.util.RandomId.generateRandomId;

//@service
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
    public PaymentServiceImpl(String JpaName)
    {
        this.paymentRepositoryImpl = new PaymentRepositoryImpl(JpaName);
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

    public PaymentEntity createPayment(String userId, String chargingSessionId, BigDecimal price) {
        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentId(generateUniquePaymentId());
        payment.setUserId(userId);
        payment.setChargingSessionId(chargingSessionId);
        payment.setPrice(price);
        // Lưu payment vào database
        if (paymentRepositoryImpl.addPayment(payment)) {
            return payment;
        }
        return null;
    }
    public boolean invoicePayment(String paymentId)
    {
        PaymentEntity invoicePayment = paymentRepositoryImpl.getPaymentById(paymentId);
        if(invoicePayment == null)
        {
            return false;
        }
        invoicePayment.setPaid(true);
        return paymentRepositoryImpl.updatePayment(invoicePayment);
    }

}

