package charging_manage_be.services.booking;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;

public interface BookingService {

    // Tạo hàm để check trước thử sẽ phải vào WaitingList hay Booking
    public Object createBookingOrWaiting(WaitingListEntity waitingRequest);
    public BookingEntity completeBooking(String bookingID);
    public BookingEntity cancelBooking(String bookingID);
    public BookingEntity processBooking(String chargingPostID);

}
