# HƯỚNG DẪN TRIỂN KHAI MOMO PAYMENT GATEWAY (UAT)

> Tài liệu này hướng dẫn **CÁCH LÀM** để tích hợp MoMo Payment từ đầu cho dự án Spring Boot

---

## 📚 MỤC LỤC

1. [Chuẩn Bị Dependencies](#bước-1-chuẩn-bị-dependencies)
2. [Cấu Hình Application Properties](#bước-2-cấu-hình-application-properties)
3. [Tạo DTOs](#bước-3-tạo-dtos)
4. [Tạo Feign Client](#bước-4-tạo-feign-client)
5. [Implement Service Layer](#bước-5-implement-service-layer)
6. [Tạo Controller](#bước-6-tạo-controller)
7. [Enable Feign Client](#bước-7-enable-feign-client)
8. [Setup Ngrok & Test](#bước-8-setup-ngrok--test)

---

## BƯỚC 1: Chuẩn Bị Dependencies

### 1.1. Thêm vào `pom.xml`

Mở file `pom.xml` và thêm các dependencies sau:

```xml
<dependencies>
    <!-- ... các dependencies khác ... -->
    
    <!-- Spring Cloud OpenFeign - để gọi API MoMo -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    
    <!-- Jackson - xử lý JSON (thường đã có sẵn) -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    
    <!-- Lombok (thường đã có sẵn) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>

<!-- Thêm dependency management cho Spring Cloud -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2023.0.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 1.2. Reload Maven

Sau khi thêm dependencies, reload Maven:
- IntelliJ: Click chuột phải vào `pom.xml` → Maven → Reload project
- Hoặc: Ctrl + Shift + O

---

## BƯỚC 2: Cấu Hình Application Properties

### 2.1. Thêm vào `src/main/resources/application.properties`

```properties
# MoMo Payment Gateway Configuration (UAT)
momo.partner-code=MOMO
momo.access-key=F8BBA842ECF85
momo.secret-key=K951B6PE1waDMi640xX08PD3vg6EkVlz
momo.end-point=https://test-payment.momo.vn/v2/gateway/api
momo.return-url=http://localhost:5173/app/payment-return
momo.ipn-url=https://YOUR_NGROK_URL/api/payment/ipn-handler
momo.request-type=captureWallet
```

### 2.2. Giải thích các tham số

| Tham số | Mô tả | Giá trị |
|---------|-------|---------|
| `partner-code` | Mã đối tác MoMo | `MOMO` (UAT default) |
| `access-key` | Key để xác thực | `F8BBA842ECF85` (UAT) |
| `secret-key` | Key để ký signature | `K951B6PE1waDMi640xX08PD3vg6EkVlz` (UAT) |
| `end-point` | URL API MoMo | `https://test-payment.momo.vn/v2/gateway/api` |
| `return-url` | URL redirect sau thanh toán | URL frontend của bạn |
| `ipn-url` | URL callback từ MoMo | **Cần dùng ngrok** (xem bước 8) |
| `request-type` | Loại thanh toán | `captureWallet` |

**⚠️ Lưu ý:** `ipn-url` cần là URL public, nên dùng ngrok khi dev localhost.

---

## BƯỚC 3: Tạo DTOs

### 3.1. Tạo CreateMomoRequestDTO

**Tạo file:** `src/main/java/[your_package]/model/dto/momo_payment/CreateMomoRequestDTO.java`

```java
package charging_manage_be.model.dto.momo_payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMomoRequestDTO {
    private String partnerCode;      // Mã đối tác
    private String requestType;      // Loại request (captureWallet)
    private String ipnUrl;           // URL callback từ MoMo
    private String redirectUrl;      // URL redirect sau thanh toán
    private String orderId;          // Mã đơn hàng (unique)
    private Long amount;             // Số tiền (VNĐ)
    private String orderInfo;        // Mô tả đơn hàng
    private String requestId;        // ID request (unique, dùng UUID)
    private String extraData;        // Dữ liệu thêm (có thể để rỗng)
    private String signature;        // Chữ ký HMAC SHA256
    private String lang = "vi";      // Ngôn ngữ (vi hoặc en)
}
```

### 3.2. Tạo CreateMomoResponseDTO

**Tạo file:** `src/main/java/[your_package]/model/dto/momo_payment/CreateMomoResponseDTO.java`

```java
package charging_manage_be.model.dto.momo_payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMomoResponseDTO {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private Long amount;
    private Long responseTime;
    private String message;          // Thông báo từ MoMo
    private Integer resultCode;      // 0 = thành công, khác 0 = lỗi
    private String payUrl;           // ⭐ URL để redirect user thanh toán
    private String deeplink;         // Deep link mở app MoMo
    private String qrCodeUrl;        // URL QR code thanh toán
}
```

**📝 Giải thích:**
- `payUrl`: Đây là URL quan trọng nhất, dùng để redirect user sang trang thanh toán MoMo
- `resultCode = 0`: Tạo payment thành công
- `deeplink`: Dùng để mở app MoMo trực tiếp (mobile)

---

## BƯỚC 4: Tạo Feign Client

### 4.1. Tạo Interface MomoAPI

**Tạo file:** `src/main/java/[your_package]/constant/MomoAPI.java`

```java
package charging_manage_be.constant;

import charging_manage_be.model.dto.momo_payment.CreateMomoRequestDTO;
import charging_manage_be.model.dto.momo_payment.CreateMomoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "momo-api", url = "${momo.end-point}")
public interface MomoAPI {
    
    @PostMapping(value = "/create",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    CreateMomoResponseDTO createPayment(@RequestBody CreateMomoRequestDTO request);
}
```

**📝 Giải thích:**
- `@FeignClient`: Tự động tạo HTTP client để gọi API
- `url = "${momo.end-point}"`: Lấy URL từ properties
- `/create`: Endpoint của MoMo để tạo payment
- Feign tự động serialize/deserialize JSON

---

## BƯỚC 5: Implement Service Layer

### 5.1. Tạo MomoService

**Tạo file:** `src/main/java/[your_package]/services/momo/MomoService.java`

```java
package charging_manage_be.services.momo;

import charging_manage_be.constant.MomoAPI;
import charging_manage_be.model.dto.momo_payment.CreateMomoRequestDTO;
import charging_manage_be.model.dto.momo_payment.CreateMomoResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class MomoService {
    
    @Autowired
    private MomoAPI momoAPI;

    // Inject các giá trị từ application.properties
    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.return-url}")
    private String returnUrl;

    @Value("${momo.ipn-url}")
    private String ipnUrl;

    @Value("${momo.request-type}")
    private String requestType;

    /**
     * Tạo payment request gửi đến MoMo
     * 
     * @param inputRequest Chứa orderId, amount, orderInfo từ client
     * @return Response từ MoMo (chứa payUrl để redirect)
     */
    public CreateMomoResponseDTO createPayment(CreateMomoRequestDTO inputRequest) {
        try {
            // 1. Build complete MoMo request với đầy đủ thông tin
            CreateMomoRequestDTO momoRequest = CreateMomoRequestDTO.builder()
                    .partnerCode(partnerCode)
                    .requestId(UUID.randomUUID().toString())  // UUID unique
                    .amount(inputRequest.getAmount())
                    .orderId(inputRequest.getOrderId())
                    .orderInfo(inputRequest.getOrderInfo())
                    .redirectUrl(returnUrl)
                    .ipnUrl(ipnUrl)
                    .requestType(requestType)
                    .extraData(inputRequest.getExtraData() != null ? inputRequest.getExtraData() : "")
                    .lang("vi")
                    .build();

            // 2. Generate signature (bắt buộc)
            String signature = generateSignature(momoRequest);
            momoRequest.setSignature(signature);

            // 3. Call MoMo API
            return momoAPI.createPayment(momoRequest);
            
        } catch (Exception e) {
            throw new RuntimeException("MoMo API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo chữ ký HMAC SHA256 theo yêu cầu của MoMo
     * ⚠️ Signature phải đúng format, nếu sai MoMo sẽ reject request
     */
    private String generateSignature(CreateMomoRequestDTO request) {
        try {
            // Tạo raw signature string
            // ⚠️ QUAN TRỌNG: Các tham số phải theo thứ tự alphabet
            String rawSignature = "accessKey=" + accessKey +
                    "&amount=" + request.getAmount() +
                    "&extraData=" + request.getExtraData() +
                    "&ipnUrl=" + request.getIpnUrl() +
                    "&orderId=" + request.getOrderId() +
                    "&orderInfo=" + request.getOrderInfo() +
                    "&partnerCode=" + request.getPartnerCode() +
                    "&redirectUrl=" + request.getRedirectUrl() +
                    "&requestId=" + request.getRequestId() +
                    "&requestType=" + request.getRequestType();

            // Tạo HMAC SHA256
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), 
                "HmacSHA256"
            );
            hmacSHA256.init(secretKeySpec);

            // Hash và convert sang hex string
            byte[] hash = hmacSHA256.doFinal(rawSignature.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating signature", e);
        }
    }
}
```

**🔑 Giải thích quan trọng về Signature:**

1. **Raw signature** phải chứa các tham số theo **thứ tự alphabet** (a-z)
2. Dùng **HMAC-SHA256** với `secret-key` để hash
3. Convert kết quả sang **hex string** (lowercase)
4. Nếu signature sai → MoMo trả về `resultCode = 10`

---

## BƯỚC 6: Tạo Controller

### 6.1. Thêm vào PaymentController

**File:** `src/main/java/[your_package]/controller/payment/PaymentController.java`

```java
package charging_manage_be.controller.payment;

import charging_manage_be.model.dto.momo_payment.CreateMomoRequestDTO;
import charging_manage_be.model.dto.momo_payment.CreateMomoResponseDTO;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.services.momo.MomoService;
import charging_manage_be.services.payments.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private MomoService momoService;
    
    @Autowired
    private PaymentService paymentService;

    /**
     * API 1: Tạo payment request
     * Frontend gọi API này để lấy payUrl, sau đó redirect user
     * 
     * Request body:
     * {
     *   "orderId": "PAY_123",
     *   "orderInfo": "Thanh toán tiền sạc xe",
     *   "extraData": ""
     * }
     */
    @PostMapping("/createPayment")
    public ResponseEntity<CreateMomoResponseDTO> createPayment(
            @RequestBody CreateMomoRequestDTO requestData) {
        try {
            // 1. Validate payment tồn tại trong DB
            PaymentEntity payment = paymentService.getPaymentByPaymentId(requestData.getOrderId());
            if (payment == null) {
                throw new RuntimeException("Payment not found: " + requestData.getOrderId());
            }

            // 2. Set amount từ DB (quan trọng để đảm bảo số tiền đúng)
            requestData.setAmount(payment.getPrice().longValue());

            // 3. Gọi MoMo API
            CreateMomoResponseDTO response = momoService.createPayment(requestData);
            
            // 4. Frontend sẽ nhận payUrl và redirect user
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Xử lý lỗi
            CreateMomoResponseDTO errorResponse = CreateMomoResponseDTO.builder()
                    .resultCode(-1)
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * API 2: IPN Handler (Instant Payment Notification)
     * MoMo gọi API này sau khi user thanh toán xong
     * ⚠️ Đây là server-to-server callback, không phải từ browser
     * 
     * MoMo sẽ gửi JSON:
     * {
     *   "orderId": "PAY_123",
     *   "resultCode": 0,
     *   "amount": 50000,
     *   ...
     * }
     */
    @PostMapping("/ipn-handler")
    public ResponseEntity<String> handleIPN(@RequestBody String ipnData) {
        try {
            // 1. Parse JSON từ MoMo
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(ipnData);
            
            String orderId = root.get("orderId").asText();
            int resultCode = root.path("resultCode").asInt();

            // 2. Log để debug
            System.out.println("IPN received: orderId=" + orderId + ", resultCode=" + resultCode);

            // 3. Kiểm tra kết quả thanh toán
            if (resultCode == 0) {
                // ✅ Thanh toán thành công
                boolean isPaid = paymentService.invoicePayment(orderId);
                
                if (isPaid) {
                    return ResponseEntity.ok("Payment successful");
                } else {
                    return ResponseEntity.status(500).body("Failed to update payment");
                }
            } else {
                // ❌ Thanh toán thất bại
                System.out.println("Payment failed with resultCode: " + resultCode);
                return ResponseEntity.status(400).body("Payment failed");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
```

**🔄 Flow hoạt động:**

```
1. Frontend gọi: POST /api/payment/createPayment
   Body: { orderId, orderInfo }
   
2. Backend → MoMo API
   MoMo trả về: { payUrl, resultCode: 0 }
   
3. Frontend nhận payUrl → Redirect user
   window.location.href = payUrl
   
4. User thanh toán trên trang MoMo
   
5. MoMo gọi callback: POST /api/payment/ipn-handler
   Backend cập nhật DB: isPaid = true
   
6. MoMo redirect user về: momo.return-url
   Frontend hiển thị kết quả
```

---

## BƯỚC 7: Enable Feign Client

### 7.1. Thêm @EnableFeignClients vào Main class

**File:** `src/main/java/[your_package]/Main.java`

```java
package charging_manage_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;  // ← Import này

@SpringBootApplication
@EnableFeignClients  // ← THÊM annotation này
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```

**⚠️ Quan trọng:** Nếu thiếu `@EnableFeignClients`, Feign Client sẽ không hoạt động!

---

## BƯỚC 8: Setup Ngrok & Test

### 8.1. Cài đặt Ngrok

1. Tải ngrok: https://ngrok.com/download
2. Giải nén và chạy:

```bash
ngrok http 8080
```

(Thay `8080` bằng port backend của bạn)

### 8.2. Cập nhật IPN URL

Sau khi chạy ngrok, bạn sẽ thấy:

```
Forwarding  https://abc123.ngrok-free.app -> http://localhost:8080
```

**Cập nhật trong `application.properties`:**

```properties
momo.ipn-url=https://abc123.ngrok-free.app/api/payment/ipn-handler
```

**⚠️ Lưu ý:** 
- Ngrok free thay đổi URL mỗi lần khởi động
- Phải restart backend sau khi đổi IPN URL

### 8.3. Test Flow

#### Bước 1: Tạo Payment trong DB

```sql
INSERT INTO payment (payment_id, price, is_paid, charging_session_id, user_id)
VALUES ('PAY_TEST001', 50000, false, 'SESSION_001', 'USER_001');
```

#### Bước 2: Gọi API tạo payment

**Request:**
```bash
POST http://localhost:8080/api/payment/createPayment
Content-Type: application/json

{
  "orderId": "PAY_TEST001",
  "orderInfo": "Test thanh toán 50k"
}
```

**Response:**
```json
{
  "partnerCode": "MOMO",
  "orderId": "PAY_TEST001",
  "amount": 50000,
  "resultCode": 0,
  "message": "Successful.",
  "payUrl": "https://test-payment.momo.vn/gw_payment/...",
  ...
}
```

#### Bước 3: Mở payUrl trong browser

Copy `payUrl` và paste vào browser

#### Bước 4: Thanh toán test

- **Số điện thoại:** `0963181714` hoặc `0909014382`
- **OTP:** `999999`

#### Bước 5: Kiểm tra IPN handler

Xem log backend, phải thấy:
```
IPN received: orderId=PAY_TEST001, resultCode=0
```

#### Bước 6: Verify DB

```sql
SELECT * FROM payment WHERE payment_id = 'PAY_TEST001';
-- Kết quả: is_paid = true
```

---

## 📌 CHECKLIST TRIỂN KHAI

- [ ] Thêm dependencies vào `pom.xml`
- [ ] Reload Maven project
- [ ] Thêm config vào `application.properties`
- [ ] Tạo `CreateMomoRequestDTO.java`
- [ ] Tạo `CreateMomoResponseDTO.java`
- [ ] Tạo `MomoAPI.java` (Feign Client)
- [ ] Tạo `MomoService.java`
- [ ] Thêm endpoints vào `PaymentController.java`
- [ ] Thêm `@EnableFeignClients` vào `Main.java`
- [ ] Cài đặt và chạy ngrok
- [ ] Cập nhật `momo.ipn-url` với ngrok URL
- [ ] Restart backend
- [ ] Test thanh toán

---

## ❗ TROUBLESHOOTING

### Lỗi 1: `No qualifying bean of type 'MomoAPI'`
**Nguyên nhân:** Thiếu `@EnableFeignClients`
**Giải pháp:** Thêm annotation vào Main class

### Lỗi 2: `resultCode = 10` (Invalid signature)
**Nguyên nhân:** Signature sai
**Giải pháp:** 
- Kiểm tra `secret-key` đúng chưa
- Kiểm tra raw signature có đúng thứ tự alphabet không

### Lỗi 3: IPN handler không được gọi
**Nguyên nhân:** 
- Ngrok chưa chạy
- IPN URL sai

**Giải pháp:**
- Chạy `ngrok http 8080`
- Cập nhật `momo.ipn-url`
- Test IPN: `curl https://[ngrok-url]/api/payment/ipn-handler`

### Lỗi 4: `Payment not found`
**Nguyên nhân:** Payment chưa tồn tại trong DB
**Giải pháp:** Tạo payment trước khi gọi API

---

## 🎯 LƯU Ý QUAN TRỌNG

1. **UAT vs Production:**
   - UAT: Dùng credentials test
   - Production: Phải đăng ký MoMo merchant chính thức

2. **Bảo mật:**
   - **KHÔNG** commit `secret-key` lên Git
   - Dùng environment variables trong production

3. **HTTPS:**
   - Production **phải** dùng HTTPS cho IPN URL
   - Ngrok free version hỗ trợ HTTPS

4. **Verify Signature:**
   - Nên verify signature từ MoMo trong IPN handler (hiện chưa implement)

---

## 📚 Tài Liệu Tham Khảo

- MoMo Developer: https://developers.momo.vn/
- MoMo UAT Test: https://test-payment.momo.vn/
- Ngrok Documentation: https://ngrok.com/docs
- Spring Cloud OpenFeign: https://spring.io/projects/spring-cloud-openfeign

---

**🎉 Chúc bạn triển khai thành công!**

