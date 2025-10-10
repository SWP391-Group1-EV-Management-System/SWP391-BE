package charging_manage_be.model.entity.charging;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.util.List;

@Entity
@Table (name = "charging_post")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingPostEntity {
    @Id
    @Column (name = "id_charging_post")
    private String idChargingPost;
    @Column (name = "is_active",nullable = false)
    private boolean isActive;
    @Column (name="maxPower", nullable = false)
    private BigDecimal maxPower;
    @Column (name="charging_fee_per_kwh", nullable = false)
    private BigDecimal chargingFeePerKWh;
    @ManyToOne
    @JoinColumn(name = "id_charging_station", nullable = false)
    private ChargingStationEntity chargingStation;
    @OneToMany(mappedBy = "chargingPost")
    private List<ChargingSessionEntity> chargingSessions;
    @ManyToMany
    @JoinTable(
            name = "charging_type_post",
            joinColumns = @JoinColumn(name = "id_charging_post"),
            inverseJoinColumns = @JoinColumn(name = "id_charging_type")
    )

    private List<ChargingTypeEntity> chargingType;
    @OneToMany(mappedBy = "chargingPost" )
    private List<WaitingListEntity> waitingList;

    @OneToMany(mappedBy = "chargingPost" )
    private List<BookingEntity> bookings;

}
