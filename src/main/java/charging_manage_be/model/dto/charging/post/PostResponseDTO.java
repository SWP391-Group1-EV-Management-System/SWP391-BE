package charging_manage_be.model.dto.charging.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDTO {
    private String idChargingPost;
    private boolean isActive;
    private BigDecimal maxPower;
    private BigDecimal chargingFeePerKWh;
    private String chargingStation;
    private List<Integer> chargingType;
    private List<String> waitingList;
    private List<String> bookings;
    private List<String> chargingSessions;
}
