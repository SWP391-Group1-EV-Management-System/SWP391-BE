package charging_manage_be.services.booking;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.repository.waiting_list.WaitingListRepository;
import charging_manage_be.services.waiting_list.WaitingListServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingServiceImpl {
    @Autowired
    private BookingRepository bookingRepository;
    private WaitingListServiceImpl waitingListRepository;
    public boolean createBooking(BookingEntity booking) {
        String PostID = booking.getChargingPost().getIdChargingPost();
        boolean isAvailable = bookingRepository.isChargingPostAvailable(PostID);
        if(!isAvailable) {
            //gọi add hàng đợi
            waitingListRepository.handleWaitingListFromBooking(booking);
            return false; // báo cho người dùng
        }
        bookingRepository.save(booking);
        return true;
    }

}
