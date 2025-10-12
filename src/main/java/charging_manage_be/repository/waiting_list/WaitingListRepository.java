package charging_manage_be.repository.waiting_list;
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface WaitingListRepository extends JpaRepository<WaitingListEntity, String> {
    Optional<WaitingListEntity> findFirstByChargingPost_IdChargingPostAndStatusOrderByCreatedAtAsc(String chargingPostID, String status);

    Optional<WaitingListEntity> findByUserAndStatus(UserEntity user, String status);
    List<WaitingListEntity> findByChargingPost(ChargingPostEntity chargingPostEntity);
    List<WaitingListEntity> findByChargingStation(ChargingStationEntity chargingStationEntity);
    List<WaitingListEntity> findByUser(UserEntity userEntity);
    List<WaitingListEntity> findByCreatedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

}