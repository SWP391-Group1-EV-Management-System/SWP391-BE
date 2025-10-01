package charging_manage_be.model.entity.cars;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "cars")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarEntity {
    @Id
    @Column(name = "car_id", nullable = false)
    private String carID; // Mã xe

    @Column(name = "license_plate", nullable = false)
    private String licensePlate; // Biển số xe

    @ManyToOne // Nhiều xe có thể được mua bởi 1 người
    @JoinColumn(name = "user_id", nullable = false) // Khóa ngoại tham chiếu đến bảng users
    // Và thêm cột user_id vào bảng cars
    private UserEntity user;

    @Column(name = "type_car", nullable = false, length = 50)
    private String typeCar; // Loại xe
    @Column(name = "chassis_number", nullable = false, length = 50)
    private String chassisNumber; // Số khung xe

    @ManyToOne
    @JoinColumn(name = "charging_type_id", nullable = false)
    private ChargingTypeEntity chargingType; // Loại sạc

    @OneToMany(mappedBy = "car" )
    private List<WaitingListEntity> waitingList;

    @OneToMany(mappedBy = "car" )
    private List<BookingEntity> bookingList;
}
