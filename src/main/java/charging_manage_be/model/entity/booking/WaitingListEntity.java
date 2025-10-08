package charging_manage_be.model.entity.booking;

import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "waiting_list")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaitingListEntity {
    @Id
    @Column(name = "waiting_list_id")
    private String waitingListId;
    @Column(name = "expected_waiting_time") // thời gian dự kiến chờ tham chiếu theo thời gian expected end bên booking
    private LocalDateTime expectedWaitingTime; // có thể lấy thời gian driver 1 đến trạm (15p) + thời gian sạc dự kiến
    @ManyToOne // Qua User để chỉnh mối quan hệ lại
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne // Qua charging Station để chỉnh mối quan hệ lại
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStationEntity chargingStation; // mã trạm sạc

    @ManyToOne // Qua charging Post để chỉnh mối quan hệ lại
    @JoinColumn(name = "charging_post_id", nullable = false)
    private ChargingPostEntity chargingPost; // mã trụ sạc

    @ManyToOne // Qua Car để chỉnh mối quan hệ lại
    @JoinColumn(name = "car_id", nullable = false)
    private CarEntity car;

    @Column(name = "outed_at", nullable = true)
    private LocalDateTime outedAt; // vị trí trong danh sách chờ

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp // so với @PrePersist thì @CreationTimestamp tự động hơn, không cần viết hàm
    private LocalDateTime createdAt; // thời gian tạo

    @Column(name = "status", nullable = false)
    private String status; // trạng thái chờ: waiting, canceled, completed

    @OneToOne(mappedBy = "waitingList")
    private BookingEntity booking;


}
