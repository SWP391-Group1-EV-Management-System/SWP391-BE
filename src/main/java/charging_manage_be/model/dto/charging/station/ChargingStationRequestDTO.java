package charging_manage_be.model.dto.charging.station;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingStationRequestDTO {
    private String nameChargingStation;
    private String address;
    private boolean isActive;
    private int numberOfPosts;
    private String userManagerId;
    private String coordinate;
}
