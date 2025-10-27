package charging_manage_be.controller.payment;


import charging_manage_be.model.dto.payment.PaymentMethodResponse;
import charging_manage_be.model.entity.payments.PaymentMethodEntity;
import charging_manage_be.services.payments.PaymentMethodService;
import charging_manage_be.services.payments.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/paymentMethod")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @PostMapping
    public ResponseEntity<PaymentMethodEntity> addPaymentMethod(@RequestBody PaymentMethodEntity paymentMethodEntity) {
        boolean isAdded = paymentMethodService.insertPaymentMethod(paymentMethodEntity.getNamePaymentMethod());
        if (isAdded) {
            return ResponseEntity.ok(paymentMethodEntity);
        } else {
            throw new RuntimeException("Unable to add payment method");
        }
    }

    @GetMapping("/{paymentMethodId}")
    public ResponseEntity<PaymentMethodResponse> getPaymentMethodById(@PathVariable String paymentMethodId) {
        boolean isExist = paymentMethodService.getPaymentMethodById(paymentMethodId).isPresent();
        if (isExist) {
            PaymentMethodEntity paymentMethod = paymentMethodService.getPaymentMethodById(paymentMethodId).get();
            PaymentMethodResponse  paymentMethodResponse = new PaymentMethodResponse();
            paymentMethodResponse.setNamePaymentMethod(paymentMethod.getNamePaymentMethod());
            paymentMethodResponse.setIdPaymentMethod(paymentMethod.getIdPaymentMethod());
            return ResponseEntity.ok(paymentMethodResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<PaymentMethodResponse>> getAllPaymentMethods() {
        List<PaymentMethodEntity> paymentMethods = paymentMethodService.getAllPaymentMethod();
        List<PaymentMethodResponse> listPaymentMethodResponse = paymentMethods.stream()
                .map(pm -> new PaymentMethodResponse(pm.getIdPaymentMethod(), pm.getNamePaymentMethod()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(listPaymentMethodResponse);
    }

    @DeleteMapping("/{paymentMethodId}")
    public ResponseEntity<String> deletePaymentMethod(@PathVariable String paymentMethodId) {
        boolean isDeleted = paymentMethodService.deletePaymentMethod(paymentMethodId);
        if (isDeleted) {
            return ResponseEntity.ok("Payment method deleted successfully");
        } else {
            return ResponseEntity.status(500).body("Failed to delete payment method");
        }
    }

    @PutMapping
    public ResponseEntity<String> updatePaymentMethod(@RequestBody PaymentMethodEntity paymentMethodEntity) {
        boolean isUpdated = paymentMethodService.updatePaymentMethod(paymentMethodEntity);
        if (isUpdated) {
            return ResponseEntity.ok("Payment method updated successfully");
        } else {
            return ResponseEntity.status(500).body("Failed to update payment method");
        }
    }
}
