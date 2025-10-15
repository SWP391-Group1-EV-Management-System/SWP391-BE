//package charging_manage_be.controller.momo;
//
//import charging_manage_be.model.dto.momo_payment.CreateMomoRequestDTO;
//import charging_manage_be.model.dto.momo_payment.CreateMomoResponseDTO;
//import charging_manage_be.model.entity.payments.PaymentEntity;
//import charging_manage_be.services.momo.MomoService;
//import charging_manage_be.services.payments.PaymentService;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//// Trong controller, giờ cần phải làm một flow như: Khi nhận được request tạo thanh toán từ frontend, gọi service để tạo payment, lưu thông tin đơn hàng vào DB bao gồm giá trị và trạng thái mặc định cho is_paid là false
//// Sau đó trả về URL thanh toán cho frontend
//// Frontend sẽ nhận được URL thanh toán từ Momo và redirect user sang đó để hoàn tất thanh toán
//// Khi Momo gọi lại IPN handler, ta sẽ cập nhật trạng thái đơn hàng
//// Và giờ trong Payment, ta sẽ thêm các trường như orderId, orderInfo, amount để lưu thông tin đơn hàng
//@RestController
//@RequestMapping("/api/momo")
//public class   MomoController {
//    @Autowired
//    private MomoService momoService;
//    @Autowired
//    private PaymentService paymentService;
//
//    @PostMapping(value = "/create-payment")
//    public ResponseEntity<CreateMomoResponseDTO> createPayment(@RequestBody CreateMomoRequestDTO requestData) {
//        try {
//            // Tức là mình sẽ lấy ra payment từ orderId của requestData(là cái mình nhập trong body của postman) rồi nó sẽ so sánh với paymentId trong bảng payment xem có tồn tại không
//            PaymentEntity payment = paymentService.getPaymentByPaymentId(requestData.getOrderId());
//            if(payment == null) {
//                throw new RuntimeException("Payment not found for orderId: " + requestData.getOrderId());
//            }
//            // Nếu có thì gán các giá trị cần thiết cho requestData
//            requestData.setOrderId(payment.getPaymentId());
//            requestData.setAmount(payment.getPrice().longValue());
//            requestData.setOrderInfo(requestData.getOrderInfo());
//
//            CreateMomoResponseDTO response = momoService.createPayment(requestData);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            // Return error response in proper format
//            CreateMomoResponseDTO errorResponse = CreateMomoResponseDTO.builder()
//                    .resultCode(-1)
//                    .message("Error: " + e.getMessage())
//                    .build();
//            return ResponseEntity.badRequest().body(errorResponse);
//        }
//    }
//
//    @PostMapping(value = "/completedPayment") // Hàm này dùng để Momo gọi lại khi có thay đổi trạng thái thanh toán
//    public ResponseEntity<String> completedPayment(@RequestBody CreateMomoRequestDTO requestData) {
//        // Parse JSON to get resultCode and orderId
//        String paymentId = requestData.getOrderId();
//        boolean isCompleted = paymentService.invoicePayment(paymentId);
//        if (isCompleted) {
//            return ResponseEntity.ok("Success");
//        }
//        else{
//            return ResponseEntity.status(500).body("Failed to complete payment");
//        }
//    }
//
//
////    @GetMapping("/order-status/{orderId}")
////    public ResponseEntity<String> getOrderStatus(@PathVariable String orderId) {
////        return orderMomoService.findByOrderMomoId(orderId)
////                .map(order -> ResponseEntity.ok("Order " + orderId + " status: " + order.getStatus()))
////                .orElse(ResponseEntity.notFound().build());
////    }
////
////    @GetMapping("/orders")
////    public ResponseEntity<List<Order>> getAllOrders() {
////        return ResponseEntity.ok(orderMomoService.getAllOrders());
////    }
//}