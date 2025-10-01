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

<<<<<<< HEAD
    @ManyToOne // Qua Car để chỉnh mối quan hệ lại
    @JoinColumn(name = "license_plate", nullable = false)
=======
    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
>>>>>>> 28dd984 (Code about Waiting and Booking Service)
    private CarEntity car;

    @Column(name = "create_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "max_waiting_time", nullable = false)
    private int maxWaitingTime;

    @Column(name = "status", nullable = false)
<<<<<<< HEAD
    private String status;
    // đã đặt
    // đang sạc
    // đã hủy
    // đã sạc xong
=======
    private String status; // booked, canceled, done

    @Column(name = "done_at", nullable = true)
    private LocalDateTime doneAt;
>>>>>>> 28dd984 (Code about Waiting and Booking Service)

    @OneToOne(mappedBy = "booking" )
    private ChargingSessionEntity chargingSession;

}
