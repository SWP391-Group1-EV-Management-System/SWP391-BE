package charging_manage_be.model.entity.charging_type;

import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging_type_post.ChargingTypePostEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table (name = "charging_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingTypeEntity {
    @Id
    private int idChargingType;
    @Column (nullable = false)
    private String nameChargingType;
    @Column (nullable = true)
    private String description;
    @OneToMany(mappedBy = "chargingType")
    private List<ChargingTypePostEntity> chargingTypeStationEntityList;

    @OneToMany (mappedBy = "chargingType")
    private List<CarEntity> carEntityList;
}
