package charging_manage_be.model.dto.charging;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class postResponse {
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
