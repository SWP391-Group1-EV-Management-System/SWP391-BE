package charging_manage_be.services.booking;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;

import java.time.LocalDateTime;

public interface BookingService {

    // Tạo hàm để check trước thử sẽ phải vào WaitingList hay Booking
     int handleBookingNavigation(String userId, String chargingPostId, String carId);
     BookingEntity completeBooking(String bookingId);
     BookingEntity cancelBooking(String bookingId);
     BookingEntity processBooking(String chargingPostId);
     boolean updateChargingBookingStatus(String bookingId);

}
