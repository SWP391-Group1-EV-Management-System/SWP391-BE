//package charging_manage_be.services.waiting_list;
//
//import charging_manage_be.model.entity.booking.WaitingListEntity;
//import charging_manage_be.model.entity.cars.CarEntity;
//import charging_manage_be.model.entity.charging.ChargingPostEntity;
//import charging_manage_be.model.entity.charging.ChargingStationEntity;
//import charging_manage_be.model.entity.users.UserEntity;
//import charging_manage_be.repository.cars.CarRepository;
//import charging_manage_be.repository.charging_station.ChargingStationRepository;
//import charging_manage_be.repository.charnging_post.ChargingPostRepository;
//import charging_manage_be.repository.users.UserRepository;
//import charging_manage_be.repository.waiting_list.WaitingListRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class WaitingListServiceImpl implements WaitingListService{
//
//    @Autowired
//    private WaitingListRepository waitingListRepository;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private ChargingStationRepository chargingStationRepository;
//    @Autowired
//    private ChargingPostRepository chargingPostRepository;
//    @Autowired
//    private CarRepository carRepository;
//
//
//
//    @Override
//    public boolean addToWaitingList(UserEntity user, ChargingStationEntity chargingStationEntity, ChargingPostEntity chargingPostEntity, CarEntity carEntity) {
//        // Check xem user có tồn tại trong UserEntity không
//        UserEntity userEntity = userRepository.findById(user.getUserID())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Check xem ChargingStation có tồn tại trong ChargingStationEntity không
//        ChargingStationEntity stationEntity = chargingStationRepository.findById(chargingStationEntity.getIdChargingStation())
//                .orElseThrow(() -> new RuntimeException("Charging Station not found"));
//
//        // Check xem ChargingPost có tồn tại trong ChargingPostEntity không
//        ChargingPostEntity postEntity = chargingPostRepository.findById(chargingPostEntity.getIdChargingPost())
//                .orElseThrow(() -> new RuntimeException("Charging Post not found"));
//
//        // Check xem Car có tồn tại trong CarEntity không
//        CarEntity car = carRepository.findById(carEntity.getLicensePlate())
//                .orElseThrow(() -> new RuntimeException("Car not found"));
//
//        WaitingListEntity waitingListEntity = new WaitingListEntity();
//        waitingListEntity.setUser(userEntity);
//        waitingListEntity.setChargingStation(chargingStationEntity);
//        waitingListEntity.setChargingPost(chargingPostEntity);
//        waitingListEntity.setCar(carEntity);
//
//        waitingListEntity.setStatus("waiting");
//        // Vị trí chờ sẽ là số lượng bản ghi hiện tại + 1
//    }
//
//    @Override
//    public boolean removeFromWaitingList(String waitingListID) {
//        return false;
//    }
//
//    @Override
//    public boolean successfulEntry(String waitingListID) {
//        return false;
//    }
//
//    @Override
//    public Optional<WaitingListEntity> findLatestByUserId(String userID) {
//        return Optional.empty();
//    }
//
//    @Override
//    public List<WaitingListEntity> findAllUserOnWaitingList() {
//        return List.of();
//    }
//}
