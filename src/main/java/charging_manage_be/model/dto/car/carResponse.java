package charging_manage_be.model.dto.car;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class carResponse {

    private String carID; // Mã xe

    private String licensePlate; // Biển số xe

    private UserEntity user;


    private String typeCar; // Loại xe

    private String chassisNumber; // Số khung xe

    private ChargingTypeEntity chargingType; // Loại sạc

    private List<WaitingListEntity> waitingList;

    private List<BookingEntity> bookingList;
}
