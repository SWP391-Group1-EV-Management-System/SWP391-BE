package charging_manage_be.services.charging_station;

import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;

public interface ChargingStationService {
    public boolean isPaymentIdExists(String id);
    public boolean addStation(String addressStation, String  statusStation, String nameStation, UserEntity userManager);
    public ChargingStationEntity getStationById(String stationId);
    public boolean updateFullStation(String stationId, String addressStation, String  statusStation, String nameStation, UserEntity userManager);

}
