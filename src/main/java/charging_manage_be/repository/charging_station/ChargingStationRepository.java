package charging_manage_be.repository.charging_station;

import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStationEntity, String> {
    ChargingStationEntity findByIdChargingStation(String idChargingStation);
    // public boolean addStation(ChargingStationEntity station);
   // public boolean updateStation(ChargingStationEntity station);
  //  public boolean isExistById(String stationId);
    //public ChargingStationEntity getStationById(String stationId);
    @Query ("SELECT c FROM ChargingStationEntity c JOIN c.chargingPosts p WHERE p.idChargingPost = :idChargingPostEntity")
    Optional<ChargingStationEntity> findStationByChargingPostEntity(@Param("idChargingPostEntity") String idChargingPostEntity);

}
