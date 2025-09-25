package charging_manage_be.model.entity.booking;

import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "booking")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingEntity {
    @Id
    @Column(name = "booking_id")
    private String bookingId;

    @OneToOne
    @JoinColumn(name = "waiting_list_id",  nullable = false)
    private WaitingListEntity waitingList;

    @ManyToOne // Cần qua UserEntity chỉnh lại mối quan hệ với Booking
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne // Cần qua ChargingStationEntity chỉnh lại mối quan hệ với Booking
    @JoinColumn(name = "id_charging_station", nullable = false)
    private ChargingStationEntity chargingStation;

    @ManyToOne // Cần qua ChargingStationEntity chỉnh lại mối quan hệ với Booking
    @JoinColumn(name = "id_charging_post", nullable = false)
    private ChargingPostEntity chargingPost;

    @Column(name = "create_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "max_waiting_time", nullable = false)
    private int maxWaitingTime;

    @Column(name = "status", nullable = false)
    private String status;

    @OneToOne(mappedBy = "booking" )
    private ChargingSessionEntity chargingSession;

}
