package charging_manage_be.services.charging_station;

import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.repository.charging_station.ChargingStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;

import java.util.List;

import static charging_manage_be.util.RandomId.generateRandomId;
@Service
public class ChargingStationServiceImpl implements  ChargingStationService {
    // cấu hình độ dài của id
    private final int characterLength = 2;
    private final int numberLength = 2;
    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Override
    public void updateNumberOfPosts(ChargingStationEntity station) {
        // cập nhật số lượng trụ theo kích thước của list trụ sạc
        // gọi hàm này sau khi thêm hoặc xóa trụ sạc
         station.setNumberOfPosts(station.getChargingPosts().size());
        chargingStationRepository.save(station);
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
    public boolean addStation(ChargingStationEntity station)
    {

        if(station == null)
        {
            return false;
        }
        station.setIdChargingStation(generateUniqueId());
        chargingStationRepository.save(station);
        return true;
    }

    @Override
    public ChargingStationEntity getStationById(String stationId)
    {
        return chargingStationRepository.findByIdChargingStation(stationId);
    }

    @Override
    public boolean updateFullStation(ChargingStationEntity station)
    {
        if(station == null || !isPaymentIdExists(station.getIdChargingStation()))
        {
            return false;
        }
        chargingStationRepository.save(station);
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
