package charging_manage_be.repository.charging_session;

import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChargingSessionRepository extends JpaRepository<ChargingSessionEntity, String> {
     //Optional<LocalDateTime> findExpectedEndTimeByChargingPostAndIsDone(ChargingPostEntity chargingPost, boolean isDone); ghi vầy SQL lấy cả tất cả các trường trong SessionEntity luôn
    @Query("SELECT c.expectedEndTime FROM ChargingSessionEntity c WHERE c.chargingPost = :chargingPost AND c.isDone = :isDone")
    Optional<LocalDateTime> findExpectedEndTimeByChargingPostAndIsDone(
            @Param("chargingPost") ChargingPostEntity chargingPost,
            @Param("isDone") boolean isDone
    );
    List<ChargingSessionEntity> findByExpectedEndTimeLessThanEqualAndEndTimeIsNull(LocalDateTime currentTime);

    List<ChargingSessionEntity> findByUserAndIsDone(UserEntity user, boolean isDone);

    List<ChargingSessionEntity> findByStationAndIsDone(ChargingStationEntity station, boolean isDone);
}
