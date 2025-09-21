package charging_manage_be.services.charging_station;

import charging_manage_be.model.entity.charging_station.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.charging_station.ChargingStationRepositoryImpl;
import charging_manage_be.repository.payments.PaymentRepositoryImpl;

import static charging_manage_be.util.RandomId.generateRandomId;

public class ChargingStationServiceImpl implements  ChargingStationService {
    // cấu hình độ dài của id
    private final int characterLength = 2;
    private final int numberLength = 2;
    private ChargingStationRepositoryImpl chargingStationRepository;
    public ChargingStationServiceImpl(String JpaName)
    {
        this.chargingStationRepository = new ChargingStationRepositoryImpl(JpaName);
    }
    public void updateNumberOfPosts(ChargingStationEntity station) {
        // cập nhật số lượng trụ theo kích thước của list trụ sạc
        // gọi hàm này sau khi thêm hoặc xóa trụ sạc


        station.setNumberOfPosts(station.getChargingPosts().size());
        chargingStationRepository.updateStation(station);
    }
    private String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (isPaymentIdExists(newId));
        return newId;
    }
    @Override
    public boolean isPaymentIdExists(String id) {
        return chargingStationRepository.isExistById(id);
    }
    @Override
    public boolean addStation(String addressStation, String  statusStation, String nameStation, UserEntity userManager)
    {
        ChargingStationEntity station = new ChargingStationEntity();
        station.setIdChargingStation(generateUniqueId());
        station.setNameChargingStation(nameStation);
        station.setAddress(addressStation);
        station.setStatus(statusStation);
        station.setUserManager(userManager);
        return chargingStationRepository.addStation(station);
    }
    @Override
    public ChargingStationEntity getStationById(String stationId)
    {
        return chargingStationRepository.getStationById(stationId);
    }
    @Override
    public boolean updateFullStation(String stationId, String addressStation, String  statusStation, String nameStation, UserEntity userManager)
    {
        ChargingStationEntity station = chargingStationRepository.getStationById(stationId);
        if(station == null)
        {
            return false;
        }
        // chỉ cập nhật những trường khác null tùy nhu cầu sử dụng mà cập nhật từng trường riêng lẻ hoặc full
        if(nameStation != null) station.setNameChargingStation(nameStation);
        if(addressStation != null ) station.setAddress(addressStation);
        if(statusStation != null ) station.setStatus(statusStation); // trạnh thái: hoạt động/ dừng hoạt động/ đang bảo trì
        if(userManager != null ) station.setUserManager(userManager);
        // cập nhật số lượng trụ theo kích thước của list chứa trụ sạc
        station.setNumberOfPosts(station.getChargingPosts().size());
        return chargingStationRepository.updateStation(station);
    }




    /*
     @Id
    private  String idChargingStation;
    @Column(name = "name_charging_station", nullable = false)
    private  String nameChargingStation;
    @Column(name = "address", nullable = false)
    private  String address;
    @Column(name = "status", nullable = false)
    private  String status;
    @Column(name = "established_time", nullable = false)
    private  LocalDateTime establishedTime;
    @Column (name = "number_of_posts", nullable = false)
    private  int numberOfPosts;
    @OneToMany(mappedBy = "chargingStation")
    private List<ChargingPostEntity> chargingPosts;
    @OneToOne
    @JoinColumn(name = "id_user_manager")
    private UserEntity UserManager;
    // Khóa ngoại bảng user
     */

}
