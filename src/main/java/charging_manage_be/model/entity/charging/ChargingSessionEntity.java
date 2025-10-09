package charging_manage_be.model.entity.charging;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "charging_session")
public class ChargingSessionEntity {

    @Id
    @Column(name = "charging_session_id")
    private String chargingSessionId;
    @Column(name = "expected_end_time") // thời gian mong muốn thi rút sạc
    private LocalDateTime expectedEndTime; // chỉ insert khi đã đến trạm và bấm bắt đầu sạc

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = true)
    private BookingEntity booking;

    @ManyToOne
    @JoinColumn(name = "charging_post_id", nullable = false)
    private ChargingPostEntity chargingPost;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStationEntity station;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    @ManyToOne
    @JoinColumn(name = "user_manage")
    private UserEntity userManage;


    @Column(name = "is_done")
    private boolean isDone;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;
    @Column(name = "kwh")
    private BigDecimal kWh;
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @OneToOne(mappedBy = "session")
    private PaymentEntity payment;
    @PrePersist
    protected void onCreate() {
        this.startTime = LocalDateTime.now();
        this.isDone = false;
    }
}