package charging_manage_be.model.entity.charging_type;

import charging_manage_be.model.entity.charging_type_post.ChargingTypePostEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table (name = "charging_type")
public class ChargingTypeEntity {
    @Id
    private int idChargingType;
    @Column (nullable = false)
    private String nameChargingType;
    @Column (nullable = true)
    private String description;
    @OneToMany(mappedBy = "chargingType")
    private List<ChargingTypePostEntity> chargingTypeStationEntityList;

    public ChargingTypeEntity() {
    }
    public ChargingTypeEntity(int idChargingType, String nameChargingType, String description) {
        this.idChargingType = idChargingType;
        this.nameChargingType = nameChargingType;
        this.description = description;
    }

    public int getIdChargingType() {
        return idChargingType;
    }

    public void setIdChargingType(int idChargingType) {
        this.idChargingType = idChargingType;
    }

    public String getNameChargingType() {
        return nameChargingType;
    }

    public void setNameChargingType(String nameChargingType) {
        this.nameChargingType = nameChargingType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ChargingTypePostEntity> getChargingTypeStationEntityList() {
        return chargingTypeStationEntityList;
    }

    public void setChargingTypeStationEntityList(List<ChargingTypePostEntity> chargingTypeStationEntityList) {
        this.chargingTypeStationEntityList = chargingTypeStationEntityList;
    }
}
