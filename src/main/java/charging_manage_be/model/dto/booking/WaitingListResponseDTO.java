package charging_manage_be.model.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaitingListResponseDTO {
    private String waitingListId;
    private LocalDateTime expectedWaitingTime;
    private String userId;
    private String chargingStationId;
    private String chargingPostId;
    private String carId;
    private LocalDateTime outedAt;
    private LocalDateTime createdAt;
    private String status;
}
