package charging_manage_be.model.dto.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingSessionResponse {
    private String chargingSessionId;
    private LocalDateTime expectedEndTime;
    private String booking;
    private String chargingPost;
    private String station;
    private String user;
    private String userManage;
    private boolean isDone;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal kWh;
    private BigDecimal totalAmount;


}
