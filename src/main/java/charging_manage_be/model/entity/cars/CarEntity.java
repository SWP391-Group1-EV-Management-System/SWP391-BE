package charging_manage_be.model.entity.cars;

import charging_manage_be.model.entity.charging_type.ChargingTypeEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cars")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarEntity {
    @Id
    private String license_plate; // Biển số xe

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
}
