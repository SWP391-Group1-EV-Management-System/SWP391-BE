package charging_manage_be.controller.charging;


import charging_manage_be.model.dto.session.ChargingSessionRequest;
import charging_manage_be.model.dto.session.ChargingSessionResponse;
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.services.booking.BookingService;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.status_service.UserStatusService;
import charging_manage_be.services.user_reputations.UserReputationService;
import charging_manage_be.services.waiting_list.WaitingListService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
    @Autowired
    private UserStatusService userStatusService;
    private final String STATUS_SESSION = "session";
    private final String STATUS_PAYMENT = "payment";
    @PostMapping("/create")
    public ResponseEntity<String> createChargingSession(@RequestBody ChargingSessionRequest createSession) { // gồm có đối tượng booking và expectedEndTime
        BookingEntity booking = bookingService.getBookingByBookingId(createSession.getBooking().getBookingId());
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
        userStatusService.setUserStatus(booking.getUser().getUserID(), STATUS_SESSION);
        return ResponseEntity.ok("Charging Session create completed successfully");
    }
    @PostMapping("/finish/{sessionId}")
    public ResponseEntity<String> endChargingSession(@PathVariable String sessionId){
        ChargingSessionEntity session = sessionService.getSessionById(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }
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
//        boolean isOnTime = session.getExpectedEndTime().isEqual(session.getEndTime());
//        if(!isOnTime){ --> Không cần check isOnTime nữa vì dù đúng giờ hay không đúng giờ thì cũng phải hỏi người trong hàng đợi và khi người trong hàng đợi đồng ý
//        thì gọi API này thủ công nên không cần check isOnTime nữa còn nếu không đồng ý thì đã có hàm xử lý việc tự động lấy người trong hàng đợi ra khi đủ giờ rồi
            bookingService.processBooking(session.getChargingPost().getIdChargingPost());
            //đã set status thành booking cho thằng tiếp theo ở trong này rồi
//        }
        // không thì phải request cho người trong hàng đợi sau có muốn vào luôn hay không
        userStatusService.setUserStatus(session.getUser().getUserID(), STATUS_PAYMENT);
        return ResponseEntity.ok("Charging Session finish completed successfully");
    }
    @GetMapping("/show/{sessionId}")
    public ResponseEntity<ChargingSessionResponse> getSessionById(@PathVariable String sessionId){
        ChargingSessionEntity session = sessionService.getSessionById(sessionId);
        ChargingSessionResponse sessionR = new ChargingSessionResponse(session.getChargingSessionId(), session.getExpectedEndTime()
                ,session.getBooking().getBookingId(),session.getChargingPost().getIdChargingPost()
                ,session.getStation().getIdChargingStation(),session.getUser().getUserID()
                ,session.getUserManage().getUserID(), session.isDone(), session.getStartTime()
                , session.getEndTime(),session.getKWh(), session.getTotalAmount() );
        if(session == null)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessionR);
    }

    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    @Transactional
    public void checkAndEndSessions() {
        // idea là ngoài việc tự bấm nút kết thúc session trước khi đủ giờ sạc
        // Thì mỗi phút hệ thống sẽ kiểm trả các session nào đã tới expectedEndTime mà chưa kết thúc thì tự động kết thúc
        List<ChargingSessionEntity> session = sessionService.findSessionsToEnd(LocalDateTime.now()); // Tìm các session có thời gian kết thúc dự kiến <= thời gian hiện tại và chưa có tgian kết thúc kết thúc
        for (ChargingSessionEntity chargingSession : session) {
            sessionService.endSession(chargingSession.getChargingSessionId());
            // nếu có booking thì gọi thằng completeBooking để hoàn thành booking
            if (chargingSession.getBooking() != null) {
                bookingService.completeBooking(chargingSession.getBooking().getBookingId());
                bookingService.processBooking(chargingSession.getChargingPost().getIdChargingPost());
                userReputationService.handleEarlyUnplugPenalty(chargingSession);
            }
            userStatusService.setUserStatus(chargingSession.getUser().getUserID(), STATUS_PAYMENT);
        }
    }
    @GetMapping("showAll/{userId}")
    public ResponseEntity<List<ChargingSessionResponse>> getAllSessionsByUserId(@PathVariable String userId){
        List<ChargingSessionEntity> sessions = sessionService.getAllSessionByUserStatusDone(userId);
        List<ChargingSessionResponse> sessionResponses = sessions.stream().map(session -> new ChargingSessionResponse(
                session.getChargingSessionId(), session.getExpectedEndTime(),
                session.getBooking() != null ? session.getBooking().getBookingId() : null,
                session.getChargingPost().getIdChargingPost(),
                session.getStation().getIdChargingStation(),
                session.getUser().getUserID(),
                session.getUserManage().getUserID(),
                session.isDone(),
                session.getStartTime(),
                session.getEndTime(),
                session.getKWh(),
                session.getTotalAmount()
        )).toList();
        return ResponseEntity.ok(sessionResponses);
    }
    // lấy tất cả session đang hoạt động theo trạm
    @GetMapping("showChargingSession/{stationId}/undone")
    public ResponseEntity<List<ChargingSessionResponse>> getAllActiveSessionsUndone(@PathVariable String stationId){
        List<ChargingSessionEntity> sessions = sessionService.getAllSessionInStationWithStatus(stationId, false);
        List<ChargingSessionResponse> sessionResponses = sessions.stream().map(session -> new ChargingSessionResponse(
                session.getChargingSessionId(), session.getExpectedEndTime(),
                session.getBooking() != null ? session.getBooking().getBookingId() : null,
                session.getChargingPost().getIdChargingPost(),
                session.getStation().getIdChargingStation(),
                session.getUser().getUserID(),
                session.getUserManage().getUserID(),
                session.isDone(),
                session.getStartTime(),
                session.getEndTime(),
                session.getKWh(),
                session.getTotalAmount()
        )).toList();
        return ResponseEntity.ok(sessionResponses);
    }
    // lấy tất cả các session đã hoàn thành theo trạm
    @GetMapping("showChargingSession/{stationId}/done")
    public ResponseEntity<List<ChargingSessionResponse>> getAllActiveSessionsDone(@PathVariable String stationId){
        List<ChargingSessionEntity> sessions = sessionService.getAllSessionInStationWithStatus(stationId, true);
        List<ChargingSessionResponse> sessionResponses = sessions.stream().map(session -> new ChargingSessionResponse(
                session.getChargingSessionId(), session.getExpectedEndTime(),
                session.getBooking() != null ? session.getBooking().getBookingId() : null,
                session.getChargingPost().getIdChargingPost(),
                session.getStation().getIdChargingStation(),
                session.getUser().getUserID(),
                session.getUserManage().getUserID(),
                session.isDone(),
                session.getStartTime(),
                session.getEndTime(),
                session.getKWh(),
                session.getTotalAmount()
        )).toList();
        return ResponseEntity.ok(sessionResponses);
    }
    // lấy tất cả session
    @GetMapping("showChargingSession/all")
    public ResponseEntity<List<ChargingSessionResponse>> getAllSessions(){
        List<ChargingSessionEntity> sessions = sessionService.getAllSessions();
        List<ChargingSessionResponse> sessionResponses = sessions.stream().map(session -> new ChargingSessionResponse(
                session.getChargingSessionId(), session.getExpectedEndTime(),
                session.getBooking() != null ? session.getBooking().getBookingId() : null,
                session.getChargingPost().getIdChargingPost(),
                session.getStation().getIdChargingStation(),
                session.getUser().getUserID(),
                session.getUserManage().getUserID(),
                session.isDone(),
                session.getStartTime(),
                session.getEndTime(),
                session.getKWh(),
                session.getTotalAmount()
        )).toList();
        return ResponseEntity.ok(sessionResponses);
    }

    /*
    ## 5. Thống kê phiên sạc/trụ/hàng chờ (Statistics)
    ### GET /api/charging/session/statistics
    - **Trả về:** Tổng số phiên sạc, số phiên đang sạc, số phiên hoàn thành, tổng doanh thu, ...
    - **Logic BE:** Tính toán số liệu tổng hợp cho dashboard staff.

    ### GET /api/charging/station/statistics
    - **Trả về:** Tổng số trụ online, offline, đang sạc, trống, ...
    - **Logic BE:** Tính toán số liệu tổng hợp cho dashboard staff.

    ---
     */
    // giả lập trụ ảo



}
