package charging_manage_be.repository.charging_station;

import charging_manage_be.model.entity.Charging.ChargingStationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStationEntity, String> {
    ChargingStationEntity findByIdChargingStation(String idChargingStation);
    // public boolean addStation(ChargingStationEntity station);
   // public boolean updateStation(ChargingStationEntity station);
  //  public boolean isExistById(String stationId);
    //public ChargingStationEntity getStationById(String stationId);
}
