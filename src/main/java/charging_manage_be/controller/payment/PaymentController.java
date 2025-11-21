package charging_manage_be.controller.payment;

import charging_manage_be.model.dto.momo_payment.CreateMomoRequestDTO;
import charging_manage_be.model.dto.momo_payment.CreateMomoResponseDTO;
import charging_manage_be.model.dto.payment.PaymentRequest;
import charging_manage_be.model.dto.payment.PaymentResponse;
import charging_manage_be.model.dto.payment.PaymentResponseDetail;
import charging_manage_be.model.dto.service_package.PackageTransactionResponseDTO;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.service_package.PackageTransactionEntity;
import charging_manage_be.model.entity.service_package.PaymentServicePackageEntity;
import charging_manage_be.services.momo.MomoService;
import charging_manage_be.services.payments.PaymentMethodService;
import charging_manage_be.services.payments.PaymentService;
import charging_manage_be.services.service_package.*;
import charging_manage_be.services.users.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Autowired
    private PaymentServicePackageService paymentPackageService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ServicePackageService servicePackageService;
    @Autowired
    private UserService  userService;
    @Autowired
    private PackageTransactionService  packageTransactionService;
    @Autowired
    private PaymentServiceAndPackageTransactionService paymentServiceAndPackageTransactionService;

    private final String  PAYMENTMOMO = "PMT_MOMO";
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
    public ResponseEntity<String> processPayment( @RequestBody PaymentRequest payment) {
        boolean isProcessed = paymentService.updatePaymentWithMethod(payment.getPaymentId(), payment.getPaymentMethodId());
        if (isProcessed) {
            return ResponseEntity.ok("Payment processed successfully");
        } else {
            return ResponseEntity.status(500).body("Failed to choose payment method");
        }
    }

    @PostMapping("/createPayment")
    public ResponseEntity<CreateMomoResponseDTO> createPayment(@RequestBody CreateMomoRequestDTO requestData) {
        try {
            // Tức là mình sẽ lấy ra payment từ orderId của requestData(là cái mình nhập trong body của postman) rồi nó sẽ so sánh với paymentId trong bảng payment xem có tồn tại không
            PaymentEntity payment = paymentService.getPaymentByPaymentId(requestData.getOrderId());
            if(payment == null) {
                //throw new RuntimeException("Payment not found for orderId: " + requestData.getOrderId());
                String userId = (String) redisTemplate.opsForHash().get("userPackage:" + requestData.getOrderId(), "userId");
                String packageId = (String) redisTemplate.opsForHash().get("userPackage:" + requestData.getOrderId(), "packetId");
                //PaymentServicePackageEntity paymentPackage = paymentPackageService.getPaymentServicePackageById(requestData.getOrderId());
                if(userId == null || packageId == null) {
                    throw new RuntimeException("Payment Service Package not found for orderId: " + requestData.getOrderId());
                }
                else {
                    //redisTemplate.opsForHash().put("userPackage:" + paymentPackage.getUser().getUserID(), "paymentPackageService", "YES");
                    requestData.setOrderId(requestData.getOrderId());
                    requestData.setAmount(servicePackageService.getPriceByPackegeId(packageId).longValue());
                    requestData.setOrderInfo(requestData.getOrderInfo());
                }
            }
            else{
                requestData.setOrderId(payment.getPaymentId());
                requestData.setAmount(payment.getPrice().longValue());
                requestData.setOrderInfo(requestData.getOrderInfo());
            }

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
            String packagePayment = null;

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(ipnData);
            String orderId = root.get("orderId").asText();
            int resultCode = root.path("resultCode").asInt();
            PaymentEntity payment = paymentService.getPaymentByPaymentId(orderId);
            //PaymentServicePackageEntity paymentPackage = paymentPackageService.getPaymentServicePackageById(orderId);
            if(payment == null) {
                packagePayment =(String) redisTemplate.opsForHash().get("userPackage:" + orderId, "paymentPackageService");
            }

            if (resultCode == 0) {
                if(packagePayment != null && packagePayment.equals("YES")) {

                    //paymentPackageService.invoicePaymentServicePackage(orderId);
                    PaymentServicePackageEntity paymentPackage = new PaymentServicePackageEntity();
                    paymentPackage.setPaymentServicePackageId(orderId);
                    paymentPackage.setPrice(servicePackageService.getPriceByPackegeId((String) redisTemplate.opsForHash().get("userPackage:" + orderId, "packetId")));
                    paymentPackage.setPaymentMethod(paymentMethodService.getPaymentMethodById(PAYMENTMOMO).orElse(null));
                    paymentPackage.setServicePackage(servicePackageService.getServicePackageByPackageId((String) redisTemplate.opsForHash().get("userPackage:" + orderId, "packetId")));
                    paymentPackage.setUser(userService.getUserByID((String) redisTemplate.opsForHash().get("userPackage:" + orderId, "userId")).orElse(null));

                    paymentServiceAndPackageTransactionService.completePackagePurchase(paymentPackage.getUser().getUserID(), paymentPackage.getServicePackage().getPackageId(), paymentPackage.getPaymentMethod().getIdPaymentMethod(), orderId);

                    redisTemplate.opsForHash().delete("userPackage:" + orderId, "userId", "packetId", "paymentPackageService");
//                  paymentPackageService.insertPaymentServicePackage(paymentPackage.getServicePackage().getPackageId(), paymentPackage.getUser().getUserID(), paymentPackage.getPaymentMethod().getIdPaymentMethod());
//                  packageTransactionService.insertPackageTransaction(paymentPackage.getUser().getUserID(), paymentPackage.getServicePackage().getPackageId(), paymentPackage.getPaymentServicePackageId());
                    return ResponseEntity.ok("Payment Service Package activated successfully");
                }



                boolean isPaid = paymentService.invoicePayment(orderId);
                if (isPaid) {
//                    assert payment != null;
//                    if (payment.getPaymentMethod().getIdPaymentMethod().equals("PMT_PACKAGE")){
//                        PackageTransactionResponseDTO packageTransaction = packageTransactionService.getLatestActivePackageByUserId(payment.getUser().getUserID());
//                        if (packageTransaction != null) {
//                            boolean quotaUpdated = packageTransactionService.updateQuotationPackageTransaction(packageTransaction.getPackageTransactionId(), payment.getSession().getChargingSessionId());
//                            if (!quotaUpdated) {
//                                return ResponseEntity.status(400).body("Failed to update quota - insufficient quota or inactive package");
//                            }
//                        }
//                    }
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
    public ResponseEntity<PaymentResponseDetail> getPaymentByPaymentId(@PathVariable String paymentId) {
        PaymentEntity payment = paymentService.getPaymentByPaymentId(paymentId);
        String paymentMethod = null;
        if (payment.getPaymentMethod() != null) {
            paymentMethod = payment.getPaymentMethod().getNamePaymentMethod();
        }

        PaymentResponseDetail paymentResponseDetail = new PaymentResponseDetail(
                payment.getPaymentId(),
                payment.getSession().getChargingSessionId(),
                payment.isPaid(),
                payment.getSession().getStation().getNameChargingStation(),
                payment.getSession().getKWh(),
                payment.getPrice(),
                paymentMethod
        );
        if (payment != null) {
            return ResponseEntity.ok(paymentResponseDetail);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/paymentByUser/{userId}")
    public ResponseEntity<List<PaymentResponseDetail>> getPaymentByUserId(@PathVariable String userId) {
        List<PaymentEntity> payments = paymentService.getPaymentByUserID(userId);
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
                payment.getPrice()
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
                payment.getPrice()
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
                payment.getPrice()
        )).toList();
        if (paymentResponses != null) {
            return ResponseEntity.ok(paymentResponses);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



}