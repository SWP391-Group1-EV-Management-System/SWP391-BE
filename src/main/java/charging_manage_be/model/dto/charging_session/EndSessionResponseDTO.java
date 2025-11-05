package charging_manage_be.model.dto.charging_session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndSessionResponseDTO {
    private boolean success;
    private String message;
    private String sessionId;

    // Thông tin về early charging offer
    private boolean hasWaitingDriver;  // Có người đang chờ không?
    private boolean sentEarlyOffer;    // Đã gửi offer sạc sớm không?
    private String nextDriverId;       // ID của driver tiếp theo (nếu có)
    private Long minutesEarly;         // Số phút rút sạc sớm
    private LocalDateTime expectedEndTime;  // Thời gian dự kiến kết thúc
    private LocalDateTime actualEndTime;    // Thời gian thực tế kết thúc

    // Thông tin session
    private Double chargedEnergy;      // Năng lượng đã sạc (kWh)
    private Double totalAmount;        // Tổng tiền
}

