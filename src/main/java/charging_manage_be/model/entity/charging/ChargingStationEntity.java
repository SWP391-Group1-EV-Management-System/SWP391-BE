package charging_manage_be.model.entity.charging;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table  (name = "charging_station")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingStationEntity {
//Ma tram/UserId quan ly/Ten Tram/Dia Chi /Trang Thái/moc thơi gian lap / so tru sac
    @Id
    @Column (name = "id_charging_station", nullable = false)
    private  String idChargingStation;
    @Column(name = "name_charging_station", nullable = false)
    private  String nameChargingStation;
    @Column(name = "address", nullable = false)
    private  String address;
    @Column(name = "is_active", nullable = false)
    private  boolean isActive;
    @Column(name = "established_time", nullable = false)
    private  LocalDateTime establishedTime;
    @Column (name = "number_of_posts", nullable = false)
    private  int numberOfPosts;
    // tọa độ trạm sạc
    @Column (name = "coordinate", nullable = true)
    private  String coordinate;

    @OneToMany(mappedBy = "chargingStation")
    private List<ChargingPostEntity> chargingPosts;

    @OneToMany(mappedBy = "station")
    private List<ChargingSessionEntity> chargingSession;

    @OneToOne
    @JoinColumn(name = "id_user_manager")
    private UserEntity UserManager;
    // Khóa ngoại bảng user
    @PrePersist
    protected void onCreate() {
        this.establishedTime = LocalDateTime.now();

    }

    @OneToMany(mappedBy = "chargingStation")
    private List<WaitingListEntity> waitingList;

    @OneToMany(mappedBy = "chargingStation")
    private List<BookingEntity> bookings;



//        public ChargingStationEntity() {
//        }
//
//        public ChargingStationEntity( String nameChargingStation, String address, String status, String establishedTime,/* int numberOfPosts,*/ UserEntity userManager) {
//            this.nameChargingStation = nameChargingStation;
//            this.address = address;
//            this.status = status;
//            //this.numberOfPosts = numberOfPosts; đếm số post tự động
//            UserManager = userManager;
//        }
}
