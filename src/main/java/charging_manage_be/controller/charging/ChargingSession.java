package charging_manage_be.controller.charging;


import charging_manage_be.model.dto.booking.BookingIdForSessionResDTO;
import charging_manage_be.model.dto.charging_session.EndSessionResponseDTO;
import charging_manage_be.model.dto.session.ChargingSessionDetail;
import charging_manage_be.model.dto.session.ChargingSessionRequest;
import charging_manage_be.model.dto.session.ChargingSessionResponse;
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.services.booking.BookingService;
import charging_manage_be.services.charging_post.ChargingPostService;
import charging_manage_be.services.charging_post.ChargingPostStatusService;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.charging_station.ChargingStationService;
import charging_manage_be.services.status_service.UserStatusService;
import charging_manage_be.services.user_reputations.UserReputationService;
import charging_manage_be.services.waiting_list.WaitingListService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    @Autowired
    private ChargingPostStatusService chargingPostStatusService;

    private final String STATUS_SESSION = "session";
    private final String STATUS_PAYMENT = "payment";

    @PostMapping("/update-preference")
    public ResponseEntity<Map<String, Object>> updateChargingPreference(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        int targetPin = (int) request.get("targetPin");
        int maxSecond = (int) request.get("maxSecond"); // Nhận số giây từ frontend (đã tính sẵn)

        // Frontend đã tính: (targetPin - currentPin) * 13.25
        // Backend chỉ cần lưu vào Redis

        // Lưu vào Redis
        sessionService.storeChargingPreference(userId, targetPin, maxSecond);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Charging preference updated successfully");
        response.put("targetPin", targetPin);
        response.put("maxSecond", maxSecond);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createChargingSession(@RequestBody ChargingSessionRequest createSession) { // gồm có đối tượng booking và expectedEndTime
        // check driver đã có booking thì check đúng trụ chưa
        // check driver nếu có waiting thì không được vào sạc
        String status = null;
        String sessionId = null;
        String postId = bookingService.getPostIdByNewBookingOfUserId(createSession.getBooking().getUser());
        WaitingListEntity waitingE = waitingService.getNewWaitingListByUserId(createSession.getBooking().getUser());
        BookingEntity bookingE = bookingService.getNewBookingByUserId(createSession.getBooking().getUser());
        // thêm trường hợp trụ này đã được booking bởi driver khác
        if(waitingE == null && bookingE == null) // nếu không có booking và waiting và trụ đó cũng không bị booking và waiting
        {
            if(chargingPostService.isPostGotBooking(createSession.getBooking().getChargingPost())) // post không có booking
            {
                BookingIdForSessionResDTO bookingSession = bookingService.getLatestConfirmedBookingByUserId(createSession.getBooking().getUser());
                LocalDateTime expectedEndTime = createSession.getExpectedEndTime();

                if (bookingSession == null) {
                    sessionId = sessionService.addSessionWithoutBooking(createSession.getBooking().getUser(), createSession.getBooking().getChargingPost(), expectedEndTime);
                } else {
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
                status = userStatusService.setUserStatus(createSession.getBooking().getUser(), STATUS_SESSION);

                // ✅ THÊM: Broadcast trạng thái trụ đang charging (nhánh không có booking)
                if (sessionId != null) {
                    chargingPostStatusService.broadcastPostStatus(createSession.getBooking().getChargingPost());
                }
            }else
            {
                status = "trụ đang bận";
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", status);
            response.put("sessionId", sessionId);
            return ResponseEntity.ok(response);  //return luôn không chạy vào else if
        }
        else if( waitingE != null || !postId.equals(createSession.getBooking().getChargingPost())) // nếu có waiting hoặc booking và booking phải đúng trụ
        {
            status = "bạn đang có đặt chỗ khác hoặc trong hàng đợi";
        } else { // nếu có booking và quét đúng trụ

            BookingIdForSessionResDTO bookingSession = bookingService.getLatestConfirmedBookingByUserId(createSession.getBooking().getUser());
            LocalDateTime expectedEndTime = createSession.getExpectedEndTime();

            if (bookingSession == null) {
                sessionId = sessionService.addSessionWithoutBooking(createSession.getBooking().getUser(), createSession.getBooking().getChargingPost(), expectedEndTime);
            } else {
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
            status = userStatusService.setUserStatus(createSession.getBooking().getUser(), STATUS_SESSION);

            // ✅ THÊM: Broadcast trạng thái trụ đang charging
            if (sessionId != null) {
                chargingPostStatusService.broadcastPostStatus(createSession.getBooking().getChargingPost());
            }
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("sessionId", sessionId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/finish/{sessionId}")
    public ResponseEntity<?> endChargingSession(@PathVariable String sessionId){
        ChargingSessionEntity session = sessionService.getSessionById(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }

        // ✅ endSession() trả về DTO với thông tin chi tiết
        EndSessionResponseDTO response = sessionService.endSession(sessionId);

        userReputationService.handleEarlyUnplugPenalty(session);

        // nếu có booking thì gọi thằng completeBooking để hoàn thành booking
        if (session.getBooking() != null) {
            bookingService.completeBooking(session.getBooking().getBookingId());
        }

        userStatusService.setUserStatus(session.getUser().getUserID(), STATUS_PAYMENT);

        // ✅ THÊM: Broadcast trạng thái trụ đã kết thúc sạc (trụ rảnh)
        chargingPostStatusService.broadcastPostStatus(session.getChargingPost().getIdChargingPost());

        // ✅ Trả về response chi tiết cho FE
        return ResponseEntity.ok(response);
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

    @Scheduled(fixedRate = 10000) // Chạy mỗi 10 giây
    @Transactional
    public void checkAndEndSessions() {
        // idea là ngoài việc tự bấm nút kết thúc session trước khi đủ giờ sạc
        // Thì mỗi phút hệ thống sẽ kiểm tra các session nào đã tới expectedEndTime mà chưa kết thúc thì tự động kết thúc
        List<ChargingSessionEntity> session = sessionService.findSessionsToEnd(LocalDateTime.now());
        for (ChargingSessionEntity chargingSession : session) {
            // ✅ endSession() ĐÃ XỬ LÝ HẾT LOGIC (bao gồm cả processBooking)
            sessionService.endSession(chargingSession.getChargingSessionId());

            // nếu có booking thì gọi thằng completeBooking để hoàn thành booking
            if (chargingSession.getBooking() != null) {
                bookingService.completeBooking(chargingSession.getBooking().getBookingId());


                userReputationService.handleEarlyUnplugPenalty(chargingSession);
            }
            userStatusService.setUserStatus(chargingSession.getUser().getUserID(), STATUS_PAYMENT);

            // ✅ THÊM: Broadcast trạng thái trụ đã kết thúc tự động (trụ rảnh)
            chargingPostStatusService.broadcastPostStatus(chargingSession.getChargingPost().getIdChargingPost());
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


    // Tạo một API để lấy ra giá trị dung lượng đã sạc được và thời gian đã sạc được kể từ lúc bắt đầu của một phiên sạc cụ thể
    @GetMapping(value = "/progress/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // produces để định nghĩa kiểu dữ liệu trả về là stream
    public SseEmitter steamProgress(@PathVariable String sessionId){
        SseEmitter emitter = new SseEmitter();

        new Thread(()->{ // khởi chạy một luồng riêng để gửi dữ liệu liên tục
            try {
                while (true) {
                    Map<Object, Object> progress = sessionService.getProgress(sessionId); // lấy từ Redis
                    emitter.send(SseEmitter.event() // gửi sự kiện SSE
                            .data(progress) // dữ liệu gửi đi
                            .name("chargingProgress")); // tên sự kiện
                    Thread.sleep(1000); // gửi mỗi giây
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start(); // khởi chạy luồng riêng để không block luồng chính
        return emitter;
    }

}
