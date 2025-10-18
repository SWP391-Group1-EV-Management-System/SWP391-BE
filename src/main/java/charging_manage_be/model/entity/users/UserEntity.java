package charging_manage_be.model.entity.users;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.reputations.UserReputationEntity;
import charging_manage_be.model.entity.service_package.PackageTransactionEntity;
import charging_manage_be.model.entity.service_package.PaymentServicePackageEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Làm cho userID tự động random 4 chữ cái và 4 chữ số
    @Id
    @Column(name = "user_id")
    private String userID;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    @Column(name = "birth_date", nullable = false, length = 10)
    private Date birthDate;
    @Column(name = "gender", nullable = false)
    private boolean gender;
    @Column(name = "role", nullable = false, length = 20)
    private String role;
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp // Dánh dấu để Hibernate tự động gán giá trị thời gian hiện tại khi bản ghi được tạo
    private LocalDateTime createdAt; // LocalDateTime thay vì Date là để lấy giờ hiện tại dễ dàng hơn

    @Column(name = "status", nullable = false, length = 20)
    private boolean status;

    // @OneToMany(mappedBy = "user, cascade = CascadeType.ALL, fetch = FetchType.LAZY")
    // cascade = CascadeType.ALL, fetch = FetchType.LAZY là để quản lý quan hệ một-nhiều giữa UserEntity và CarEntity

    @OneToMany(mappedBy = "user" ) // mappedBy = "userID" là tên thuộc tính trong CarEntity tham chiếu đến UserEntity
    //Trong dòng "private UserEntity userID" ở CarEntity thì userID trong mapped ở đây chính là userID trong CarEntity
    // Và userID đó là khóa ngoại tham chiếu đến UserEntity từ CarEntity
    private List<CarEntity> cars;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<UserReputationEntity> userReputations;

    @OneToOne(mappedBy = "UserManager")
    private ChargingStationEntity chargingStation;

    @OneToMany(mappedBy = "user")
    private List<PaymentEntity> payments;

    @OneToMany(mappedBy = "user" )
    private List<WaitingListEntity> waitingLists;

    @OneToMany (mappedBy = "user")
    private List<BookingEntity> bookings;

    @OneToMany(mappedBy = "user")
    private List<ChargingSessionEntity> userSession;
    @OneToMany(mappedBy = "userManage")
    private List<ChargingSessionEntity> userManagerSession;
    @OneToMany(mappedBy = "user")
    private List<PackageTransactionEntity>  packageTransactions;
    @OneToMany(mappedBy = "user")
    private List<PaymentServicePackageEntity> paymentServicePackages;
}
