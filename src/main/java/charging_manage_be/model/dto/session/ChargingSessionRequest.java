package charging_manage_be.model.dto.session;

import charging_manage_be.model.dto.booking.BookingRequestDTO;
import charging_manage_be.model.entity.booking.BookingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingSessionRequest {
    private BookingRequestDTO booking;
    private LocalDateTime expectedEndTime;
}
