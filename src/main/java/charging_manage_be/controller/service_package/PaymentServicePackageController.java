package charging_manage_be.controller.service_package;

import charging_manage_be.model.dto.service_package.PaymentServicePackageCreate;
import charging_manage_be.services.payments.PaymentMethodService;
import charging_manage_be.services.service_package.PaymentServicePackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-service-packages")
public class PaymentServicePackageController {

    @Autowired
    private PaymentServicePackageService paymentServicePackageService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/CreatePaymentPacket")
    public String createIdPaymentPacket(@RequestBody PaymentServicePackageCreate paymentServicePackageCreate)
    {
        String paymenPacketId = paymentServicePackageService.generateUniquePaymentId();
        Map<String, String> map = new HashMap<>();
        map.put("paymentPackageService", "YES");
        map.put("userId", paymentServicePackageCreate.getUserId());
        map.put("packetId", paymentServicePackageCreate.getPackageId());
        redisTemplate.opsForHash().putAll("userPackage:" + paymenPacketId, map);
        return paymenPacketId;
    }

}
