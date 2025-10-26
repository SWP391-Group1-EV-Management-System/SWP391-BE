package charging_manage_be.model.dto.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingSessionDetail {
    private String chargingSessionId;
    private LocalDateTime expectedEndTime;
    private String booking;
    private String chargingPost;
    private String station;
    private String stationName;
    private String addressStation;
    private BigDecimal pricePerKWH;
    private BigDecimal maxPower;
    private List<String> typeCharging;
    private String user;
    private String userManage;
    private LocalDateTime startTime;
}
/*
tên trạm
địa chỉ trạm
giá tiền kwh
công xuất tối đa
loại cổng sạc
 */