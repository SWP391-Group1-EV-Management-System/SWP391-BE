package charging_manage_be.model.dto.car;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarResponseDTO {
    private String carID;
    private String licensePlate;
    private String user;
    private String typeCar;
    private String chassisNumber;
    private int chargingType;
    private List<String> waitingList;
    private List<String> bookingList;
}
