package charging_manage_be.controller.payment;

import charging_manage_be.model.dto.momo_payment.CreateMomoRequestDTO;
import charging_manage_be.model.dto.momo_payment.CreateMomoResponseDTO;
import charging_manage_be.model.dto.payment.PaymentResponse;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.services.momo.MomoService;
import charging_manage_be.services.payments.PaymentMethodService;
import charging_manage_be.services.payments.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentMethodService paymentMethodService;
    @Autowired
    private MomoService momoService;
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

    @PostMapping("/createPayment")
    public ResponseEntity<CreateMomoResponseDTO> createPayment(@RequestBody CreateMomoRequestDTO requestData) {
        try {
            // Tức là mình sẽ lấy ra payment từ orderId của requestData(là cái mình nhập trong body của postman) rồi nó sẽ so sánh với paymentId trong bảng payment xem có tồn tại không
            PaymentEntity payment = paymentService.getPaymentByPaymentId(requestData.getOrderId());
            if(payment == null) {
                throw new RuntimeException("Payment not found for orderId: " + requestData.getOrderId());
            }
            // Nếu có thì gán các giá trị cần thiết cho requestData
            requestData.setOrderId(payment.getPaymentId());
            requestData.setAmount(payment.getPrice().longValue());
            requestData.setOrderInfo(requestData.getOrderInfo());

            CreateMomoResponseDTO response = momoService.createPayment(requestData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return error response in proper format
            CreateMomoResponseDTO errorResponse = CreateMomoResponseDTO.builder()
                    .resultCode(-1)
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/ipn-handler")
    public ResponseEntity<String> handleIPN(@RequestBody String ipnData) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(ipnData);
            String orderId = root.get("orderId").asText();
            int resultCode = root.path("resultCode").asInt();

            if (resultCode == 0) {
                boolean isPaid = paymentService.invoicePayment(orderId);
                if (isPaid) {
                    return ResponseEntity.ok("Payment successfully");
                }
                else{
                    return ResponseEntity.status(500).body("Failed to process payment");
                }
            }
            else{
                return ResponseEntity.status(500).body("Failed to process payment");
            }

        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping(value = "/completedPayment") // Hàm này dùng để Momo gọi lại khi có thay đổi trạng thái thanh toán
    public ResponseEntity<String> completedPayment(@RequestBody CreateMomoRequestDTO requestData) {
        // Parse JSON to get resultCode and orderId
        String paymentId = requestData.getOrderId();
        boolean isCompleted = paymentService.invoicePayment(paymentId);
        if (isCompleted) {
            return ResponseEntity.ok("Success");
        }
        else{
            return ResponseEntity.status(500).body("Failed to complete payment");
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentByPaymentId(@PathVariable String paymentId) {
        PaymentEntity payment = paymentService.getPaymentByPaymentId(paymentId);
        PaymentResponse paymentResponse = new PaymentResponse(
                payment.getPaymentId(),
                payment.getUser().getUserID(),
                payment.getSession().getChargingSessionId(),
                payment.isPaid(),
                payment.getCreatedAt(),
                payment.getPaidAt(),
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().getIdPaymentMethod() : null,
                payment.getPrice(),
                payment.getSession().getChargingSessionId()
        );
        if (payment != null) {
            return ResponseEntity.ok(paymentResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/paymentByUser/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentByUserId(@PathVariable String userId) {
        List<PaymentEntity> payments = paymentService.getPaymentByUserID(userId);
        List<PaymentResponse> paymentResponses = payments.stream().map(payment -> new PaymentResponse(
                payment.getPaymentId(),
                payment.getUser().getUserID(),
                payment.getSession().getChargingSessionId(),
                payment.isPaid(),
                payment.getCreatedAt(),
                payment.getPaidAt(),
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().getIdPaymentMethod() : null,
                payment.getPrice(),
                payment.getSession().getChargingSessionId()
        )).toList();
        if (paymentResponses != null) {
            return ResponseEntity.ok(paymentResponses);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/all")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentEntity> payments = paymentService.findAllPayment();
        List<PaymentResponse> paymentResponses = payments.stream().map(payment -> new PaymentResponse(
                payment.getPaymentId(),
                payment.getUser().getUserID(),
                payment.getSession().getChargingSessionId(),
                payment.isPaid(),
                payment.getCreatedAt(),
                payment.getPaidAt(),
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().getIdPaymentMethod() : null,
                payment.getPrice(),
                payment.getSession().getChargingSessionId()
        )).toList();

        return ResponseEntity.ok(paymentResponses);
    }
    @GetMapping("/paymentByUser/UnPaid/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentByUserIdUnPaid(@PathVariable String userId) {
        List<PaymentEntity> payments = paymentService.findUnpaidPaymentsUser(userId);
        List<PaymentResponse> paymentResponses = payments.stream().map(payment -> new PaymentResponse(
                payment.getPaymentId(),
                payment.getUser().getUserID(),
                payment.getSession().getChargingSessionId(),
                payment.isPaid(),
                payment.getCreatedAt(),
                payment.getPaidAt(),
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().getIdPaymentMethod() : null,
                payment.getPrice(),
                payment.getSession().getChargingSessionId()
        )).toList();
        if (paymentResponses != null) {
            return ResponseEntity.ok(paymentResponses);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/paymentByUser/Paid/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentByUserIdPaid(@PathVariable String userId) {
        List<PaymentEntity> payments = paymentService.findPaidPaymentsUser(userId);
        List<PaymentResponse> paymentResponses = payments.stream().map(payment -> new PaymentResponse(
                payment.getPaymentId(),
                payment.getUser().getUserID(),
                payment.getSession().getChargingSessionId(),
                payment.isPaid(),
                payment.getCreatedAt(),
                payment.getPaidAt(),
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().getIdPaymentMethod() : null,
                payment.getPrice(),
                payment.getSession().getChargingSessionId()
        )).toList();
        if (paymentResponses != null) {
            return ResponseEntity.ok(paymentResponses);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



}
