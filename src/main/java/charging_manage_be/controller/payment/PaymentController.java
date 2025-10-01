package charging_manage_be.controller.payment;

import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.services.payments.PaymentMethodService;
import charging_manage_be.services.payments.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
//        boolean isCreated = paymentService.addPayment(payment.getChargingSessionId());
//        if (isCreated) {
//            return ResponseEntity.ok(payment);
//        } else {
//            throw new RuntimeException("Unable to create payment");
//        }
//    }

    @PostMapping("/paymentMethod")
    public ResponseEntity<String> processPayment( @RequestBody PaymentEntity payment) {
        boolean isProcessed = paymentService.processPayment(payment.getPaymentId(), payment.getPaymentMethod().getIdPaymentMethod());
        if (isProcessed) {
            return ResponseEntity.ok("Payment processed successfully");
        } else {
            return ResponseEntity.status(500).body("Failed to process payment");
        }
    }

    @PostMapping("/invoice/{paymentId}")
    public ResponseEntity<String> completePayment(@PathVariable String paymentId) {
        boolean isCompleted = paymentService.invoicePayment(paymentId);
        if (isCompleted) {
            return ResponseEntity.ok("Payment completed successfully");
        } else {
            return ResponseEntity.status(500).body("Failed to complete payment");
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentEntity> getPaymentByPaymentId(@PathVariable String paymentId) {
        PaymentEntity payment = paymentService.getPaymentByPaymentId(paymentId);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/paymentByUser/{userId}")
    public ResponseEntity<PaymentEntity> getPaymentByUserId(@PathVariable String userId, String paymentId) {
        PaymentEntity payment = paymentService.getPaymentByUserID(userId, paymentId);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }




}
