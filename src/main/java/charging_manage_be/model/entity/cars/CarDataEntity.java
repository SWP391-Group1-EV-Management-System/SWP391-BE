package charging_manage_be.model.entity.cars;

import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "car_data")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_data_id")
    private int carDataId;
    @Column(name= "car_name")
    private String carName;
    @Column(name="charging_type")
    private String chargingType;


    public CarDataEntity(String carName, String chargingType) {
        this.carName = carName;
        this.chargingType = chargingType;
    }
}
