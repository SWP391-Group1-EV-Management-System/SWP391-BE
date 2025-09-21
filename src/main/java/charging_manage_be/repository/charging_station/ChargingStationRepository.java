package charging_manage_be.repository.charging_station;

import charging_manage_be.model.entity.charging_station.ChargingStationEntity;

public interface ChargingStationRepository {
    public boolean addStation(ChargingStationEntity station);
    public boolean updateStation(ChargingStationEntity station);
    public boolean isExistById(String stationId);
    public ChargingStationEntity getStationById(String stationId);
}
