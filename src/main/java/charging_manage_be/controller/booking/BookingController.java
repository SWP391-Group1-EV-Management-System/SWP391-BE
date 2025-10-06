package charging_manage_be.controller.booking;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.services.booking.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<?> processBooking(@RequestBody BookingEntity booking) { // ? có nghĩa là có thể là Booking hoặc WaitingList
        int result = bookingService.handleBookingNavigation(booking.getUser().getUserID(), booking.getChargingPost().getIdChargingPost(), booking.getCar().getCarID()); // Trả về một result có thể là Booking hoặc WaitingList
        return ResponseEntity.ok(result);
    }

    @PostMapping("/complete/{bookingID}")
    public ResponseEntity<String> completeBooking(@PathVariable String bookingID) {
        BookingEntity completedBooking = bookingService.completeBooking(bookingID);
        if (completedBooking != null) {
            return ResponseEntity.ok("Booking completed successfully" );
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/cancel/{bookingID}")
    public ResponseEntity<String> cancelBooking(@PathVariable String bookingID) {
        BookingEntity booking =  bookingService.cancelBooking(bookingID);
        if (booking != null) {
            return ResponseEntity.ok("Booking cancelled successfully");
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

//    @PostMapping("/process/{chargingPostID}") // này không cần thiết vì sẽ tự động xử lý khi hoàn thành hoặc hủy booking
//    public ResponseEntity<String> processNextBooking(@PathVariable String chargingPostID) {
//        BookingEntity processedBooking = bookingService.processBooking(chargingPostID);
//        if (processedBooking != null) {
//            return ResponseEntity.ok("Processed next booking successfully");
//        } else {
//            return ResponseEntity.ok("No pending bookings to process");
//        }
//    }
}
