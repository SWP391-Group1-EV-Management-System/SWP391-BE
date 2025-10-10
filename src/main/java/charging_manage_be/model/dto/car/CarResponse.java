package charging_manage_be.model.dto.car;

import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.model.entity.users.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarResponse {
    private String carID;
    private String licensePlate;
    private String user;
    private String typeCar;
    private String chassisNumber;
    private int chargingType;
    private List<String> waitingList;
    private List<String> bookingList;
}
