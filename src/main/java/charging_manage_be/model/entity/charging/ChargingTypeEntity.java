package charging_manage_be.model.entity.charging;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_charging_type")
    private int idChargingType;
    @Column (name ="name_charging_type", nullable = false)
    private String nameChargingType;
    @Column (nullable = true)
    private String description;
    @ManyToMany(mappedBy = "chargingType")
    private List<ChargingPostEntity> chargingTypeStationEntityList;


}
