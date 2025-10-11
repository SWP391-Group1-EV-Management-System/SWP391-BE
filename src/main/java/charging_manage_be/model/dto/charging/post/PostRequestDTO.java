package charging_manage_be.model.dto.charging.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDTO {
    private String stationId;
    private boolean isActive;
    private List<Integer> listType;
    private BigDecimal maxPower;
    private BigDecimal chargingFeePerKWh;
}
