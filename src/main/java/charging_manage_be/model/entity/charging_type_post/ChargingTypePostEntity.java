package charging_manage_be.model.entity.charging_type_post;

import charging_manage_be.model.entity.charging_post.ChargingPostEntity;
import charging_manage_be.model.entity.charging_type.ChargingTypeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "charging_type_station")
public class ChargingTypePostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idChargingType")
    private ChargingTypeEntity chargingType;

    @ManyToOne
    @JoinColumn(name = "idChargingPost")
    private ChargingPostEntity chargingPost;
    public ChargingTypePostEntity() {
    }
    public ChargingTypePostEntity(ChargingTypeEntity chargingType, ChargingPostEntity chargingPost) {
        this.chargingType = chargingType;
        this.chargingPost = chargingPost;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChargingPostEntity getChargingPost() {
        return chargingPost;
    }

    public void setChargingPost(ChargingPostEntity chargingPost) {
        this.chargingPost = chargingPost;
    }

    public ChargingTypeEntity getChargingType() {
        return chargingType;
    }

    public void setChargingType(ChargingTypeEntity chargingType) {
        this.chargingType = chargingType;
    }
}
