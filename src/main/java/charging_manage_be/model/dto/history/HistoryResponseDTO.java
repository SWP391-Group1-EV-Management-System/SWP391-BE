package charging_manage_be.model.dto.history;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryResponseDTO {
    private String sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal kWh;
    private BigDecimal totalAmount;
    private boolean isDone;
    private HistoryStationDTO station;
    private HistoryPostDTO post;
    private HistoryPaymentDTO payment;
}
/*
{
  "sessionId": "CHG-20251023-001",
  "startTime": "2025-10-23T09:15:00",
  "endTime": "2025-10-23T10:02:00",
  "kWh": 12.5,
  "totalAmount": 45000,
  "isDone": true,
  "station": {
    "name": "Trạm Sạc FPT Hòa Lạc",
    "address": "Km29, Đại lộ Thăng Long, Hòa Lạc, Hà Nội"
  },
  "post": {
    "id": "POST-03",
    "maxPower": 50
  },
  "payment": {
    "isPaid": true,
    "method": "Chuyển khoản",
    "paidAt": "2025-10-23T10:05:00"
  }
}
 */