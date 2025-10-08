package charging_manage_be.controller.booking;

import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.services.booking.BookingService;
import charging_manage_be.services.waiting_list.WaitingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waiting-list")
@RequiredArgsConstructor

public class WaitingListController {
    private final WaitingListService waitingListService;
    private final BookingService bookingService;
    // tạo API request người dùng có muôn vào sạc luôn hay không
    // nếu có thì gọi API này
    // còn nếu không thì chờ đến đủ giờ rồi gọi API này tự động
    @PostMapping("/add/{chargingPostId}")
    public ResponseEntity<String> addToWaitingList(@PathVariable String chargingPostId){
        bookingService.processBooking(chargingPostId);
        return ResponseEntity.ok("User accepted to charge");
    }
    @PostMapping("/cancel/{waitingListId}")
    public ResponseEntity<String> cancelWaitingList(@PathVariable String waitingListId){
        waitingListService.cancelWaiting(waitingListId);
        return ResponseEntity.ok("Cancelled waiting successfully");
    }

    @GetMapping("/queue/{chargingPostId}" )
    public ResponseEntity<List<String>> getWaitingListForPost(@PathVariable String chargingPostId) {
        List<String> waitingList = waitingListService.getWaitingListForPost(chargingPostId);
        return ResponseEntity.ok(waitingList);
    }
}
