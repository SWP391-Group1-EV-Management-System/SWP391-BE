package charging_manage_be.model.dto.charging.station;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingStationResponseDTO {
    private String idChargingStation;
    private String nameChargingStation;
    private String address;
    private boolean isActive;
    private LocalDateTime establishedTime;
    private int numberOfPosts;
    private String userManagerName;
    private String coordinate;
    private List<String> chargingPostIds;
    private List<String> chargingSessionIds;
}
