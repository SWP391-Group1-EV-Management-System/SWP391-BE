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

                public CreateMomoResponseDTO createPayment(CreateMomoRequestDTO inputRequest) {
                    try {
                        // Create complete MoMo request
                        CreateMomoRequestDTO momoRequest = CreateMomoRequestDTO.builder()
                                .partnerCode(partnerCode)
                                .requestId(UUID.randomUUID().toString())
                                .amount(inputRequest.getAmount())
                                .orderId(inputRequest.getOrderId())
                                .orderInfo(inputRequest.getOrderInfo())
                                .redirectUrl(returnUrl)
                                .ipnUrl(ipnUrl)
                                .requestType(requestType)
                                .extraData(inputRequest.getExtraData() != null ? inputRequest.getExtraData() : "")
                                .lang("vi")
                                .build();

                        // Generate signature
                        String signature = generateSignature(momoRequest);
                        momoRequest.setSignature(signature);

                        return momoAPI.createPayment(momoRequest);
                    } catch (Exception e) {
                        throw new RuntimeException("MoMo API call failed: " + e.getMessage(), e);
                    }
                }

                private String generateSignature(CreateMomoRequestDTO request) {
                    try {
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

                        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
                        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                        hmacSHA256.init(secretKeySpec);

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