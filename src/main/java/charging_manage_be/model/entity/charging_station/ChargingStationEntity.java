package charging_manage_be.model.entity.charging_station;

import charging_manage_be.model.entity.charging_post.ChargingPostEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table  (name = "charging_station")
public class ChargingStationEntity {
//Ma tram/UserId quan ly/Ten Tram/Dia Chi /Trang Thái/moc thơi gian lap / so tru sac
    @Id
    private  String idChargingStation;
    @Column(name = "name_charging_station", nullable = false)
    private  String nameChargingStation;
    @Column(name = "address", nullable = false)
    private  String address;
    @Column(name = "status", nullable = false)
    private  String status;
    @Column(name = "established_time", nullable = false)
    private  String establishedTime;
    @Column (name = "number_of_posts", nullable = false)
    private  int numberOfPosts;
    @OneToMany(mappedBy = "chargingStation")
    private List<ChargingPostEntity> chargingPosts;
    // Khóa ngoại bảng user
    //private  String idUserManager;
    public ChargingStationEntity() {
    }

    public ChargingStationEntity(List<ChargingPostEntity> chargingPosts, String status, String address, String nameChargingStation) {
        this.chargingPosts = chargingPosts;
        this.status = status;
        this.address = address;
        this.nameChargingStation = nameChargingStation;
    }

    public List<ChargingPostEntity> getChargingPosts() {
        return chargingPosts;
    }

    public void setChargingPosts(List<ChargingPostEntity> chargingPosts) {
        this.chargingPosts = chargingPosts;
    }

    public int getNumberOfPosts() {
        return numberOfPosts;
    }

    public void setNumberOfPosts(int numberOfPosts) {
        this.numberOfPosts = numberOfPosts;
    }

    public String getEstablishedTime() {
        return establishedTime;
    }

    public void setEstablishedTime(String establishedTime) {
        this.establishedTime = establishedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNameChargingStation() {
        return nameChargingStation;
    }

    public void setNameChargingStation(String nameChargingStation) {
        this.nameChargingStation = nameChargingStation;
    }

    public String getIdChargingStation() {
        return idChargingStation;
    }

    public void setIdChargingStation(String idChargingStation) {
        this.idChargingStation = idChargingStation;
    }
}
