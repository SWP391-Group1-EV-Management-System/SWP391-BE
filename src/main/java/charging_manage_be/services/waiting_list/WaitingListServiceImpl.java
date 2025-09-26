package charging_manage_be.services.waiting_list;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.repository.waiting_list.WaitingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class WaitingListServiceImpl {
    private int characterLength = 5;
    private int numberLength = 5;
    @Autowired
    private BookingRepository bookingRepository;
    private WaitingListRepository waitingListRepository;
    public String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (isIdExists(newId));
        return newId;
    }
    public boolean isIdExists(String id) {
        return waitingListRepository.existsById(id);
    }
    public boolean handleWaitingListFromBooking(BookingEntity booking) {




        WaitingListEntity waiting = new WaitingListEntity();// timestemp tự tạo lấy để xét vị trí của tài xế
        waiting.setWaitingListId(generateUniqueId());
        waiting.setUser(booking.getUser());
        waiting.setChargingPost(booking.getChargingPost());
        waiting.setChargingStation(booking.getChargingStation());
        // gọi hàm update thứ tự sạc
        waiting.setCar(booking.getCar());
        waitingListRepository.save(waiting);

    }// sủ dụng queue
    public int getWaitingPosition(String waitingPost) { // trã về vị trí hàng đợi của driver ( cập nhật liên tục)
        // update lại id của hàng đợi là long

        return 0;
    }

}
