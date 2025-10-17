package charging_manage_be.services.charging_station;

import charging_manage_be.model.dto.charging.station.ChargingStationRequestDTO;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.charging_station.ChargingStationRepository;
import charging_manage_be.repository.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static charging_manage_be.util.RandomId.generateRandomId;
@Service
public class ChargingStationServiceImpl implements  ChargingStationService {
    // cấu hình độ dài của id
    private final int characterLength = 2;
    private final int numberLength = 2;
    @Autowired
    private ChargingStationRepository chargingStationRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public ChargingStationEntity updateNumberOfPosts(ChargingStationEntity station) {
        // cập nhật số lượng trụ theo kích thước của list trụ sạc
        // gọi hàm này sau khi thêm hoặc xóa trụ sạc
         station.setNumberOfPosts(station.getChargingPosts().size());
        chargingStationRepository.save(station);
        return station;
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
        return chargingStationRepository.existsById(id);
    }

    @Override
    public boolean addStation(ChargingStationRequestDTO station)
    {
        UserEntity manager = userRepository.findById(station.getUserManagerId()).orElse(null);
        ChargingStationEntity newStation = new ChargingStationEntity();
        newStation.setIdChargingStation(generateUniqueId());
        newStation.setNameChargingStation(station.getNameChargingStation());
        newStation.setAddress(station.getAddress());
        newStation.setActive(station.isActive());
        newStation.setUserManager(manager);
        newStation.setNumberOfPosts(station.getNumberOfPosts());
        newStation.setCoordinate(station.getCoordinate());
        chargingStationRepository.save(newStation);
        return true;
    }

    @Override
    public ChargingStationEntity getStationById(String stationId)
    {
        return chargingStationRepository.findByIdChargingStation(stationId);
    }

    @Override
    public boolean updateStation(String stationId, ChargingStationRequestDTO stationRequestDTO){
        ChargingStationEntity chargingStation = chargingStationRepository.findByIdChargingStation(stationId);
        if(chargingStation == null){
            throw new RuntimeException("Station not found with id: " + stationId);
        }
        UserEntity manager = userRepository.findById(stationRequestDTO.getUserManagerId()).orElse(null);
        chargingStation.setUserManager(manager);
        chargingStation.setAddress(stationRequestDTO.getAddress());
        chargingStation.setActive(stationRequestDTO.isActive());
        chargingStation.setNameChargingStation(stationRequestDTO.getNameChargingStation());
        chargingStation.setNumberOfPosts(stationRequestDTO.getNumberOfPosts());
        chargingStation.setCoordinate(stationRequestDTO.getCoordinate());
        chargingStationRepository.save(chargingStation);
        return true;
    }

    @Override
    public List<ChargingStationEntity> getAllStations()
    {
        return chargingStationRepository.findAll();
    }
    @Override
    public List<ChargingPostEntity> getAllPostsInStation(String stationId)
    {
        ChargingStationEntity station = chargingStationRepository.findByIdChargingStation(stationId);
        if(station == null)
        {
            return null;
        }
        return station.getChargingPosts();
    }




}
