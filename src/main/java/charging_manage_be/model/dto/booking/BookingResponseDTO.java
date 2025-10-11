package charging_manage_be.model.dto.booking;

import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDTO {
    private String bookingId;
    private String waitingListId;
    private String userId;
    private String chargingStationId;
    private String chargingPostId;
    private String carId;
    private LocalDateTime createdAt;
    private int maxWaitingTime;
    private String status;
    private LocalDateTime arrivalTime;
}
