package charging_manage_be.controller.charging;


import charging_manage_be.model.dto.session.ChargingSessionRequest;
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.services.booking.BookingService;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.user_reputations.UserReputationService;
import charging_manage_be.services.waiting_list.WaitingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/charging/session")
public class ChargingSession {

    @Autowired
    private ChargingSessionService sessionService;
    @Autowired
    private WaitingListService waitingService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserReputationService userReputationService;

    @PostMapping("/create")
    public ResponseEntity<String> createChargingSession(@RequestBody ChargingSessionRequest createSession) { // gồm có đối tượng booking và expectedEndTime
        BookingEntity booking = createSession.getBooking();
        LocalDateTime expectedEndTime = createSession.getExpectedEndTime();
        // gọi thằng waiting ở sau lưng nếu có để cập nhật addExpectedWaitingTime
        waitingService.addExpectedWaitingTime(booking.getChargingPost().getIdChargingPost(), expectedEndTime);
        if (booking.getBookingId() == null) {
            sessionService.addSessionWithoutBooking(booking.getUser().getUserID(), booking.getChargingPost().getIdChargingPost(),expectedEndTime);
        } else {
            sessionService.addSessionWithBooking(booking.getBookingId(), expectedEndTime);
            //cập nhật trạng thái bên booking thành charging ngay khi tạo session thành công
            bookingService.updateChargingBookingStatus(booking.getBookingId());

           // sau đó gọi lại hàm completeBooking ở dưới khi kết thúc session
        }
        // yêu cầu FE xử lý khi realtime đạt tới expectedEndTime thì gọi API finish ở dưới
        return ResponseEntity.ok("Charging Session create completed successfully");
    }
    @PostMapping("/finish/{sessionId}")
    public ResponseEntity<String> endChargingSession(@PathVariable String sessionId){
        ChargingSessionEntity session = sessionService.getSessionById(sessionId);
        sessionService.endSession(sessionId);
        // nếu có booking thì gọi thằng completeBooking để hoàn thành booking
        if (session.getBooking() == null) {
            return ResponseEntity.ok("Charging Session finish completed successfully");
        }
        bookingService.completeBooking(session.getBooking().getBookingId());
        userReputationService.handleEarlyUnplugPenalty(session);
        // theo flow mới ( khi chưa đủ giờ) phải hỏi driver trong waiting rằng có muốn sạc luôn hay không, nếu đồng ý thì chuyển từ waiting ra
        // không muôn sạc luôn thì phải chờ đến giờ
        // khi đã đủ giờ tự động driver trong hàng đợi sẽ được lấy ra
        // so sánh giờ dự kiến kết thúc với giờ hiện tại
        // nếu đủ thì lấy driver trong waiting list ra luôn
        boolean isOnTime = session.getExpectedEndTime().isEqual(session.getEndTime());
        if(!isOnTime){
            bookingService.processBooking(session.getChargingPost().getIdChargingPost());
        }
        // không thì phải request cho người trong hàng đợi sau có muốn vào luôn hay không
        return ResponseEntity.ok("Charging Session finish completed successfully");
    }
}
