package charging_manage_be.controller.payment;

import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.services.payments.PaymentMethodService;
import charging_manage_be.services.payments.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentMethodService paymentMethodService;
// hàm end session tự động tạo
//    @PostMapping
//    public ResponseEntity<PaymentEntity> createPayment(@RequestBody PaymentEntity payment) {
//        boolean isCreated = paymentService.addPayment(payment.getChargingSessionId(), payment.getPaymentMethod().getIdPaymentMethod(), payment.getPrice());
//        if (isCreated) {
//            return ResponseEntity.ok(payment);
//        } else {
//            return ResponseEntity.status(500).build();
//        }
//    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentEntity> getPaymentByPaymentId(String paymentId) {
        PaymentEntity payment = paymentService.getPaymentByPaymentId(paymentId);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/paymentByUser/{userId}")
    public ResponseEntity<PaymentEntity> getPaymentByUserId(String userId, String paymentId) {
        PaymentEntity payment = paymentService.getPaymentByUserID(userId, paymentId);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}
