package charging_manage_be.model.dto.dashboard;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardOfStaff {
    int totalSessionInStation;
    int totalSessionIsProcessingInStation;
    int totalSessionCompletedInStation;
    BigDecimal totalRevenueInStation;

    String chargingSessionId;
    String userName;
    String postName;
    LocalDateTime dateCreated;
    boolean status;
    LocalDateTime startedTime;
    LocalDateTime endedTime;
    BigDecimal kWh;
    BigDecimal totalAmount;
}
