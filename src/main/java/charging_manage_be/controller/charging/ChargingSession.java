package charging_manage_be.controller.charging;


import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.services.booking.BookingService;
import charging_manage_be.services.charging_session.ChargingSessionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/charging/session")
public class ChargingSession {

    @Autowired
    private ChargingSessionServiceImpl sessionService;

    @PostMapping("/create")
    public ResponseEntity<String> createChargingSession(@RequestBody String bookingId,
                                                        String userId,
                                                        String postId) {
        if (bookingId == null) {
            sessionService.addSessionWithoutBooking(userId, postId);
        } else {
            sessionService.addSessionWithBooking(bookingId);
        }

        return ResponseEntity.ok("Charging Session create completed successfully");
    }
    @PostMapping("/finish/{bookingId}")
    public ResponseEntity<String> createChargingSession(@PathVariable String bookingId){
        sessionService.endSession(bookingId);

        return ResponseEntity.ok("Charging Session finish completed successfully");
    }
}
