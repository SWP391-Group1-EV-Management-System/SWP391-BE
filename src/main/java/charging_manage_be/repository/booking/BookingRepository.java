package charging_manage_be.repository.booking;

import charging_manage_be.model.entity.booking.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BookingRepository extends JpaRepository <BookingEntity, String> {
    // hàm kiểm tra xem booking một trụ nhất định có đang được book trong bảng booking không, tức status = Reserved or Charging
//    @Query("SELECT COUNT(u) > 0  FROM BookingEntity u WHERE u.chargingPost = :chargingPost AND u.status IN ('Reserved', 'Charging') ")
//    boolean isChargingPostAvailable(
//            @Param("chargingPost") String chargingPost
//            //@Param("ten") phai trung với :ten ở query
//    );
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN false ELSE true END " +
            "FROM BookingEntity u " +
            "WHERE u.chargingPost = :chargingPost " +
            "AND u.status IN ('Reserved', 'Charging')")
    boolean isChargingPostAvailable(@Param("chargingPost") String chargingPost);

}
