package charging_manage_be.controller.charging;


import charging_manage_be.model.dto.booking.BookingIdForSessionResDTO;
import charging_manage_be.model.dto.session.ChargingSessionDetail;
import charging_manage_be.model.dto.session.ChargingSessionRequest;
import charging_manage_be.model.dto.session.ChargingSessionResponse;
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.services.booking.BookingService;
import charging_manage_be.services.charging_post.ChargingPostService;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.charging_station.ChargingStationService;
import charging_manage_be.services.status_service.UserStatusService;
import charging_manage_be.services.user_reputations.UserReputationService;
import charging_manage_be.services.waiting_list.WaitingListService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private ChargingStationService chargingStationService;
    @Autowired
    private ChargingPostService chargingPostService;

    private final String STATUS_SESSION = "session";
    private final String STATUS_PAYMENT = "payment";
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createChargingSession(@RequestBody ChargingSessionRequest createSession) { // gồm có đối tượng booking và expectedEndTime
        BookingIdForSessionResDTO bookingSession = bookingService.getLatestConfirmedBookingByUserId(createSession.getBooking().getUser());
        LocalDateTime expectedEndTime = createSession.getExpectedEndTime();
        String sessionId;
        if (bookingSession == null) {
            sessionId = sessionService.addSessionWithoutBooking(createSession.getBooking().getUser(), createSession.getBooking().getChargingPost(),expectedEndTime);
        }
        else {
        BookingEntity booking = bookingService.getBookingByBookingId(bookingSession.getBookingId());
        String bookingId = booking.getBookingId();
        // gọi thằng waiting ở sau lưng nếu có để cập nhật addExpectedWaitingTime
        waitingService.addExpectedWaitingTime(createSession.getBooking().getChargingPost(), expectedEndTime);
        bookingService.updateChargingBookingStatus(bookingId);
        sessionId = sessionService.addSessionWithBooking(bookingId, expectedEndTime);
        //cập nhật trạng thái bên booking thành charging ngay khi tạo session thành côngbookingService.updateChargingBookingStatus(booking.getBookingId());
            // sau đó gọi lại hàm completeBooking ở dưới khi kết thúc session
        }
        // yêu cầu FE xử lý khi realtime đạt tới expectedEndTime thì gọi API finish ở dưới
        String status = userStatusService.setUserStatus(createSession.getBooking().getUser(), STATUS_SESSION);
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("sessionId", sessionId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/finish/{sessionId}")
    public ResponseEntity<String> endChargingSession(@PathVariable String sessionId, @RequestBody BigDecimal kWh){
        ChargingSessionEntity session = sessionService.getSessionById(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }
        session.setKWh(kWh);
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
    public ResponseEntity<ChargingSessionDetail> getSessionById(@PathVariable String sessionId){
        String booking = "";

        ChargingSessionEntity session = sessionService.getSessionById(sessionId);
        if(session.getBooking() != null)
        {
            booking = session.getBooking().getBookingId();
        }
        ChargingStationEntity station = chargingStationService.getStationById(session.getStation().getIdChargingStation());
        ChargingPostEntity post = chargingPostService.getChargingPostById(session.getChargingPost().getIdChargingPost());
        List<String> listType = post.getChargingType().stream()
                .map(ct -> ct.getNameChargingType())
                .collect(Collectors.toList());
        ChargingSessionDetail sessionR = new ChargingSessionDetail(
                session.getChargingSessionId()
                ,session.getExpectedEndTime()
                ,booking
                ,session.getChargingPost().getIdChargingPost()
                ,session.getStation().getIdChargingStation()
                ,station.getNameChargingStation()
                ,station.getAddress()
                ,post.getChargingFeePerKWh()
                ,post.getMaxPower()
                ,listType
                ,session.getUser().getUserID()
                ,session.getUserManage().getUserID()
                ,session.getStartTime());
        if(session == null)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessionR);
    }/*
    public class ChargingSessionDetail {
    private String chargingSessionId; 1
    private LocalDateTime expectedEndTime;1
    private String booking;1
    private String chargingPost;1
    private String station; 1
    private String stationName;
    private String addressStation;
    private BigDecimal pricePerKWH;
    private BigDecimal maxPower;
    private List<String> typeCharging;
    private String user;
    private String userManage;
    private LocalDateTime startTime;
}
    */

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
    @GetMapping("/showAll/{userId}")
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
    @GetMapping("/showChargingSession/{stationId}/undone")
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
    @GetMapping("/showChargingSession/{stationId}/done")
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
    @GetMapping("/showChargingSession/all")
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
}
