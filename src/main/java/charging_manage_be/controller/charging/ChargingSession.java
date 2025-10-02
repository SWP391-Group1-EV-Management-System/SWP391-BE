package charging_manage_be.controller.charging;


import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.services.charging_session.ChargingSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/charging/session")
public class ChargingSession {

    @Autowired
    private ChargingSessionService sessionService;

    @PostMapping("/create")
    public ResponseEntity<String> createChargingSession( @RequestBody BookingEntity booking) {
        if (booking.getBookingId() == null) {
            sessionService.addSessionWithoutBooking(booking.getUser().getUserID(), booking.getChargingPost().getIdChargingPost());
        } else {
            sessionService.addSessionWithBooking(booking.getBookingId());
        }

        return ResponseEntity.ok("Charging Session create completed successfully");
    }
    @PostMapping("/finish/{sessionId}")
    public ResponseEntity<String> endChargingSession(@PathVariable String sessionId){
        sessionService.endSession(sessionId);
        return ResponseEntity.ok("Charging Session finish completed successfully");
    }
}
