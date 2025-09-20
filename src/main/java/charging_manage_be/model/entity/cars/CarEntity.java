package charging_manage_be.model.entity.cars;

import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "cars")
public class CarEntity {
    @Id
    private String license_plate; // Biển số xe

    @ManyToOne // Nhiều xe có thể thuộc về một người dùng
    @JoinColumn(name = "user_id", nullable = false) // Khóa ngoại tham chiếu đến bảng users
    // Với thêm cột user_id trong bảng cars để liên kết với bảng users qua khóa chính userID
    private UserEntity user; // Đây là khóa ngoại tham chiếu đến UserEntity

    @Column(name = "type_car", nullable = false, length = 50)
    private String typeCar; // Loại xe
    @Column(name = "chassis_number", nullable = false, length = 50)
    private String chassisNumber; // Số khung xe



}
