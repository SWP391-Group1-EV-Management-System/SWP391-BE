package charging_manage_be.repository.charging_session;

import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    List<ChargingSessionEntity> findByUser(UserEntity user);

    Optional<ChargingSessionEntity> findTopByChargingPostAndIsDoneOrderByStartTimeDesc(
            ChargingPostEntity chargingPost,
            boolean isDone
    );

    ChargingSessionEntity findFirstByChargingPostAndIsDoneFalse(ChargingPostEntity chargingPost);

    List<ChargingSessionEntity> findByIsDoneFalse();

    // src/main/java/charging_manage_be/repository/charging_session/ChargingSessionRepository.java
    @Query("SELECT COUNT(s) FROM ChargingSessionEntity s WHERE s.startTime BETWEEN :start AND :end")
    long countSessionsInMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // JpaRepository already provides:
    long count();


    ChargingSessionEntity findFirstByChargingPost_IdChargingPostOrderByStartTimeDesc(String chargingPostIdChargingPost);

    ChargingSessionEntity findFirstByChargingPost_IdChargingPostAndIsDoneOrderByStartTimeDesc(String chargingPostIdChargingPost, boolean isDone);

    @Query("SELECT SUM(s.kWh) FROM ChargingSessionEntity s WHERE s.user = :user AND s.isDone = true")
    BigDecimal sumFinishedKwhByUser(@Param("user") UserEntity user);

    int countByUserAndIsDone(UserEntity user, boolean isDone);
}
