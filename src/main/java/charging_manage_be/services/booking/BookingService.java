package charging_manage_be.services.booking;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;

public interface BookingService {

    // Tạo hàm để check trước thử sẽ phải vào WaitingList hay Booking
    public int handleBookingNavigation(String userId, String chargingPostId, String carId);
    public BookingEntity completeBooking(String bookingId);
    public BookingEntity cancelBooking(String bookingId);
    public BookingEntity processBooking(String chargingPostId);

}
