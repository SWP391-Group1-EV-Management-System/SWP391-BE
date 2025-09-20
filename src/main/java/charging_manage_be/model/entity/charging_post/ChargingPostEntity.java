package charging_manage_be.model.entity.charging_post;

import charging_manage_be.model.entity.charging_station.ChargingStationEntity;
import charging_manage_be.model.entity.charging_type_post.ChargingTypePostEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table (name = "charging_post")
public class ChargingPostEntity {
    @Id
    private String idChargingPost;
    @Column (nullable = false)
    private String status;
    @Column (name="maxPower", nullable = false)
    private BigDecimal maxPower;
    @Column (name="charging_fee_per_kwh", nullable = false)
    private BigDecimal chargingFeePerKWh;
    @ManyToOne
    @JoinColumn(name = "id_charging_station", nullable = false)
    private ChargingStationEntity chargingStation;
    @OneToMany(mappedBy = "chargingPost")
    private List<ChargingTypePostEntity> chargingTypeStationEntityList;
    public ChargingPostEntity() {
    }
    public ChargingPostEntity(String idChargingPost, String status, BigDecimal maxPower, BigDecimal chargingFeePerKWh, ChargingStationEntity chargingStation) {
        this.idChargingPost = idChargingPost;
        this.status = status;
        this.maxPower = maxPower;
        this.chargingFeePerKWh = chargingFeePerKWh;
        this.chargingStation = chargingStation;
    }

    public String getIdChargingPost() {
        return idChargingPost;
    }

    public void setIdChargingPost(String idChargingPost) {
        this.idChargingPost = idChargingPost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(BigDecimal maxPower) {
        this.maxPower = maxPower;
    }

    public BigDecimal getChargingFeePerKWh() {
        return chargingFeePerKWh;
    }

    public void setChargingFeePerKWh(BigDecimal chargingFeePerKWh) {
        this.chargingFeePerKWh = chargingFeePerKWh;
    }

    public ChargingStationEntity getChargingStation() {
        return chargingStation;
    }

    public void setChargingStation(ChargingStationEntity chargingStation) {
        this.chargingStation = chargingStation;
    }

    public List<ChargingTypePostEntity> getChargingTypeStationEntityList() {
        return chargingTypeStationEntityList;
    }

    public void setChargingTypeStationEntityList(List<ChargingTypePostEntity> chargingTypeStationEntityList) {
        this.chargingTypeStationEntityList = chargingTypeStationEntityList;
    }
}
