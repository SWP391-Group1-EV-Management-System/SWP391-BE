package charging_manage_be.services.charging_station;

import charging_manage_be.model.dto.charging.station.ChargingStationRequestDTO;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;

import java.util.List;

public interface ChargingStationService {
    public ChargingStationEntity updateNumberOfPosts(ChargingStationEntity station);
    public boolean isPaymentIdExists(String id);
    public boolean addStation(ChargingStationRequestDTO station);
    public ChargingStationEntity getStationById(String stationId);
    public boolean updateStation(String stationId, ChargingStationRequestDTO stationRequestDTO);
    public List<ChargingStationEntity> getAllStations();
    public List<ChargingPostEntity> getAllPostsInStation(String stationId);
}
