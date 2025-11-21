package charging_manage_be.controller.staff;

import charging_manage_be.model.dto.payment.PaymentResponseDetail;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.services.payments.PaymentService;
import charging_manage_be.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffDashboardController {
    @Autowired
    private PaymentService  paymentService;
    @Autowired
    private UserService userService;


    @PostMapping( "/staffConfirmPayment/{paymentId}") // Staff gọi thanh toán khi driver trã bằng tiền mặt
    public ResponseEntity<String> staffConfirmPayment(@PathVariable String paymentId) {
        if(paymentService.getPaymentByPaymentId(paymentId) == null) {
            return ResponseEntity.status(500).body("Payment not found");
        }
        boolean isCompleted = paymentService.invoicePayment(paymentId);
        if (isCompleted) {
            return ResponseEntity.ok("Success");
        }
        else{
            return ResponseEntity.status(500).body("Failed to complete payment");
        }
    }
    @GetMapping("/paymentRequestCash/{staffId}")
    public ResponseEntity<List<PaymentResponseDetail>> paymentRequestCash(@PathVariable String staffId) {
        String stationId = userService.getUserByID(staffId).get().getChargingStation().getIdChargingStation();
        if(stationId == null) {
            return ResponseEntity.status(500).body(new ArrayList<PaymentResponseDetail>());
        }
        List<PaymentEntity> payments = paymentService.getListPaymentPaymentCashByStationId(stationId);
        List<PaymentResponseDetail> paymentResponseDetail = payments.stream().map(payment -> {
            String paymentMethod = null;
            if (payment.getPaymentMethod() != null) {
                paymentMethod = payment.getPaymentMethod().getNamePaymentMethod();
            }
            return new PaymentResponseDetail(
                    payment.getPaymentId(),
                    payment.getSession().getChargingSessionId(),
                    payment.isPaid(),
                    payment.getSession().getStation().getNameChargingStation(),
                    payment.getSession().getKWh(),
                    payment.getPrice(),
                    paymentMethod
            );
        }).toList();
        if (!paymentResponseDetail.isEmpty()) {
            return ResponseEntity.ok(paymentResponseDetail);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
