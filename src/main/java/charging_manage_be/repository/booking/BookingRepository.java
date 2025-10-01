package charging_manage_be.repository.booking;

import charging_manage_be.model.entity.booking.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
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

=======
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, String> {
    Optional<BookingEntity>findFirstByChargingPost_IdChargingPostAndStatusInOrderByCreatedAtAsc(String chargingPostID, List<String> status);
    List<BookingEntity> findByUser_UserID(String userID);
    // Trạng thái của booking sẽ có các trạng thái: "waiting", "charging", "completed", "canceled"
    // Khi user tạo booking thì trạng thái sẽ là "waiting"
    // Khi đến lượt user sạc thì trạng thái sẽ chuyển thành "charging"

    // Muốn lấy trạng thái mới nhất của trạm sạc thì ta sẽ lấy theo ID trạm sạc và sắp xếp theo thời gian tạo giảm dần, lấy phần tử đầu tiên
    // Nếu khi user sạc xong thì sẽ chuyển trạng thái của booking thành "completed"
    // Khi user hủy thì sẽ chuyển trạng thái của booking thành "canceled"
    // Và trạng thái của booking là completed hoặc canceled thì sẽ lấy trạng thái mới nhất bằng cách:
    // findFirstByChargingPost_IdChargingPostAndStatusInOrderByCreatedAtDesc(chargingPostID, List.of("completed", "canceled")
>>>>>>> 28dd984 (Code about Waiting and Booking Service)
}
