package charging_manage_be.model.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationAndPost {
    private String idChargingStation;
    private String nameChargingStation;
    private String address;
    private boolean isActive;
    private LocalDateTime establishedTime;
    private int numberOfPosts;
    private double latitude;
    private double longitude;
    private Map<String,Boolean> postAvailable;
    private Double distanceKm; // Khoảng cách từ vị trí người dùng (km)
}
