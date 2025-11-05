package charging_manage_be.model.dto.charging.station;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private double latitude;
    private double longitude;
    private Map<String,Boolean> chargingPostsAvailable;
    private List<String> chargingSessionIds;
}
