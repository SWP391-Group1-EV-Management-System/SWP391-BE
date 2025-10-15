package charging_manage_be.services.booking;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    // Tạo hàm để check trước thử sẽ phải vào WaitingList hay Booking
     int handleBookingNavigation(String userId, String chargingPostId, String carId);
     BookingEntity completeBooking(String bookingId);
     BookingEntity cancelBooking(String bookingId);
     BookingEntity processBooking(String chargingPostId);
     boolean updateChargingBookingStatus(String bookingId);
    List<BookingEntity> getBookingByPostId(String postId);
    List<BookingEntity> getBookingByStationId(String stationId);
    List<BookingEntity> getBookingByUserId(String userId);
    List<BookingEntity> getBookingByCreatedDate(LocalDateTime startOfDay, LocalDateTime endOfDay);
    BookingEntity getBookingByWaitingListId(String waitingListId);
    BookingEntity getBookingByBookingId(String bookingId);
    List<BookingEntity> getBookingByStatus(String status);
    List<BookingEntity> getExpiredBookings(LocalDateTime currentTime);

}
