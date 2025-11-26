package charging_manage_be.services.charging_station;

import charging_manage_be.model.dto.agent.LocationRequest;
import charging_manage_be.model.dto.charging.station.ChargingStationRequestDTO;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;

import javax.xml.stream.Location;
import java.util.List;

public interface ChargingStationService {
    public ChargingStationEntity updateNumberOfPosts(ChargingStationEntity station);
    public boolean isPaymentIdExists(String id);
    public boolean addStation(ChargingStationRequestDTO station);
    public ChargingStationEntity getStationById(String stationId);
    public boolean updateStation(String stationId, ChargingStationRequestDTO stationRequestDTO);
    public List<ChargingStationEntity> getAllStations();
    public List<ChargingPostEntity> getAllPostsInStation(String stationId);
    List<ChargingStationEntity> getAllStationAvailable();
    List<ChargingStationEntity> findNearestStations(LocationRequest request);
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);

    long countTotalActiveStations();

    ChargingStationEntity getStationByUserId(String userId);
    ChargingStationEntity getStationByChargingPost(ChargingPostEntity chargingPost);

    boolean deactivateStation(String stationId);
    String getStationMostSession();
    String getStationLestSession();
    long getStationSessionAmout(String  stationId);
}
