package charging_manage_be.model.entity.booking;

import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
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
    @JoinColumn(name = "waiting_list_id",  nullable = true)
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

    @ManyToOne // Qua Car để chỉnh mối quan hệ lại
    @JoinColumn(name = "license_plate", nullable = false)
    private CarEntity car;

    @Column(name = "create_at", nullable = false)
    //@CreationTimestamp set thủ công cho an toàn
    private LocalDateTime createdAt;
    @Column(name = "done_at", nullable = true)
    private LocalDateTime doneAt;

    @Column(name = "max_waiting_time", nullable = false)
    private int maxWaitingTime;

    @Column(name = "status", nullable = false)
    private String status;
    // đã đặt
    // đã hủy
    // đã đến
    // hết giờ

    @OneToOne(mappedBy = "booking" )
    private ChargingSessionEntity chargingSession;

}
