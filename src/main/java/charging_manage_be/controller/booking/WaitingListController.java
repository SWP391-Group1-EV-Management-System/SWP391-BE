package charging_manage_be.controller.booking;

import charging_manage_be.model.dto.booking.WaitingListResponseDTO;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.services.booking.BookingService;
import charging_manage_be.services.charging_post.ChargingPostService;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.charging_station.ChargingStationService;
import charging_manage_be.services.waiting_list.WaitingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/waiting-list")
@RequiredArgsConstructor

public class WaitingListController {
    private final WaitingListService waitingListService;
    private final BookingService bookingService;
    private final ChargingSessionService  chargingSessionService;
    private final RedisTemplate<String, String> redisTemplate;

    // tạo API request người dùng có muôn vào sạc luôn hay không
    // nếu có thì gọi API này
    // còn nếu không thì chờ đến đủ giờ rồi gọi API này tự động
    @PostMapping("/add/{chargingPostId}")
    public ResponseEntity<String> addToWaitingList(@PathVariable String chargingPostId) {
        bookingService.processBooking(chargingPostId);
        return ResponseEntity.ok("User accepted to charge");
    }

    @PostMapping("/cancel/{waitingListId}")
    public ResponseEntity<String> cancelWaitingList(@PathVariable String waitingListId) {
        waitingListService.cancelWaiting(waitingListId);
        return ResponseEntity.ok("Cancelled waiting successfully");
    }

    // ✅ ENDPOINT MỚI: Driver B đồng ý sạc sớm
    @PostMapping("/accept-early-charging/{userId}/{chargingPostId}")
    public ResponseEntity<String> acceptEarlyCharging(
            @PathVariable String userId,
            @PathVariable String chargingPostId) {

        // Kiểm tra xem user có phải là người đầu tiên trong hàng đợi không
        String firstInQueue = redisTemplate.opsForList().index("queue:post:" + chargingPostId, 0);

        if (firstInQueue == null || !firstInQueue.equals(userId)) {
            return ResponseEntity.badRequest().body("Bạn không phải người đầu tiên trong hàng đợi");
        }

        // Chuyển driver vào booking ngay lập tức
        bookingService.processBooking(chargingPostId);

        return ResponseEntity.ok("Đã chuyển bạn vào booking. Vui lòng đến trạm sạc!");
    }

    // ✅ ENDPOINT MỚI: Driver B từ chối sạc sớm (giữ nguyên vị trí, chờ đến giờ)
    @PostMapping("/decline-early-charging/{userId}/{chargingPostId}")
    public ResponseEntity<String> declineEarlyCharging(
            @PathVariable String userId,
            @PathVariable String chargingPostId) {

        // Kiểm tra xem user có trong hàng đợi không
        List<String> queue = redisTemplate.opsForList().range("queue:post:" + chargingPostId, 0, -1);

        if (queue == null || !queue.contains(userId)) {
            return ResponseEntity.badRequest().body("Bạn không có trong hàng đợi");
        }

        System.out.println("✅ [DECLINE-EARLY] User " + userId + " declined early charging offer for post: " + chargingPostId);
        System.out.println("✅ [DECLINE-EARLY] User will be automatically moved to booking when expectedWaitingTime is reached");

        // Không làm gì cả, driver B vẫn ở vị trí đầu và sẽ tự động được chuyển khi đến giờ
        // Scheduled task sẽ tự động xử lý khi đến expectedWaitingTime
        return ResponseEntity.ok("Bạn sẽ được thông báo khi đến giờ dự kiến");
    }

    @GetMapping("/queue/post/{chargingPostId}")
    public ResponseEntity<List<WaitingListResponseDTO>> getWaitingListForPost(@PathVariable String chargingPostId) {
        List<WaitingListEntity> waitingList = waitingListService.getWaitingListForPost(chargingPostId);

        if (waitingList == null || waitingList.isEmpty()) {
            return ResponseEntity.ok(List.of()); // Trả về empty list thay vì error
        }

        List<WaitingListResponseDTO> response = waitingList.stream().map(waitingListEntity -> {
            WaitingListResponseDTO dto = new WaitingListResponseDTO();
            dto.setWaitingListId(waitingListEntity.getWaitingListId());
            dto.setExpectedWaitingTime(waitingListEntity.getExpectedWaitingTime());

            // ✅ TÍNH TOÁN THỜI GIAN CÒN LẠI CHÍNH XÁC
            if (waitingListEntity.getExpectedWaitingTime() != null) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expectedTime = waitingListEntity.getExpectedWaitingTime();

                long seconds = Duration.between(now, expectedTime).getSeconds();

                // Nếu đã quá giờ thì set = 0
                if (seconds < 0) {
                    seconds = 0;
                }

                dto.setRemainingSeconds(seconds);
                dto.setRemainingTimeFormatted(formatSeconds(seconds));
            }

            dto.setUserId(waitingListEntity.getUser().getUserID());
            dto.setChargingStationId(waitingListEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(waitingListEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(waitingListEntity.getCar().getCarID());
            dto.setOutedAt(waitingListEntity.getOutedAt());
            dto.setCreatedAt(waitingListEntity.getCreatedAt());
            dto.setStatus(waitingListEntity.getStatus());
            dto.setStationName(waitingListEntity.getChargingStation().getNameChargingStation());
            return dto;
        }).toList();
        return ResponseEntity.ok(response);
    }

    // ✅ THÊM HELPER METHOD
    private String formatSeconds(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    // Cho thêm path để lấy  hàn chờ theo trạm, lấy theo ngày tháng năm, lấy theo userID

    //Lấy theo trạm
    @GetMapping("/queue/station/{chargingStationId}")
    public ResponseEntity<List<WaitingListResponseDTO>> getWaitingListForStation(@PathVariable String chargingStationId) {
        List<WaitingListEntity> waitingList = waitingListService.getWaitingListForStation(chargingStationId);

        if (waitingList == null || waitingList.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<WaitingListResponseDTO> response = waitingList.stream().map(waitingListEntity -> {
            WaitingListResponseDTO dto = new WaitingListResponseDTO();
            dto.setWaitingListId(waitingListEntity.getWaitingListId());
            dto.setExpectedWaitingTime(waitingListEntity.getExpectedWaitingTime());

            // ✅ TÍNH TOÁN THỜI GIAN CÒN LẠI CHÍNH XÁC
            if (waitingListEntity.getExpectedWaitingTime() != null) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expectedTime = waitingListEntity.getExpectedWaitingTime();
                long seconds = Duration.between(now, expectedTime).getSeconds();
                if (seconds < 0) seconds = 0;
                dto.setRemainingSeconds(seconds);
                dto.setRemainingTimeFormatted(formatSeconds(seconds));
            }

            dto.setUserId(waitingListEntity.getUser().getUserID());
            dto.setChargingStationId(waitingListEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(waitingListEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(waitingListEntity.getCar().getCarID());
            dto.setOutedAt(waitingListEntity.getOutedAt());
            dto.setCreatedAt(waitingListEntity.getCreatedAt());
            dto.setStatus(waitingListEntity.getStatus());
            dto.setStationName(waitingListEntity.getChargingStation().getNameChargingStation());
            return dto;
        }).toList();
        return ResponseEntity.ok(response);
    }

    // Lấy theo userID
    @GetMapping("/queue/users/{userId}")
    public ResponseEntity<List<WaitingListResponseDTO>> getWaitingListForUser(@PathVariable String userId) {
        List<WaitingListEntity> waitingList = waitingListService.getWaitingListForUser(userId);

        if (waitingList == null || waitingList.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<WaitingListResponseDTO> response = waitingList.stream().map(waitingListEntity -> {
            WaitingListResponseDTO dto = new WaitingListResponseDTO();
            dto.setWaitingListId(waitingListEntity.getWaitingListId());
            dto.setExpectedWaitingTime(waitingListEntity.getExpectedWaitingTime());

            // ✅ TÍNH TOÁN THỜI GIAN CÒN LẠI CHÍNH XÁC
            if (waitingListEntity.getExpectedWaitingTime() != null) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expectedTime = waitingListEntity.getExpectedWaitingTime();
                long seconds = Duration.between(now, expectedTime).getSeconds();
                if (seconds < 0) seconds = 0;
                dto.setRemainingSeconds(seconds);
                dto.setRemainingTimeFormatted(formatSeconds(seconds));
            }

            dto.setUserId(waitingListEntity.getUser().getUserID());
            dto.setChargingStationId(waitingListEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(waitingListEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(waitingListEntity.getCar().getCarID());
            dto.setOutedAt(waitingListEntity.getOutedAt());
            dto.setCreatedAt(waitingListEntity.getCreatedAt());
            dto.setStatus(waitingListEntity.getStatus());
            dto.setStationName(waitingListEntity.getChargingStation().getNameChargingStation());
            return dto;
        }).toList();
        return ResponseEntity.ok(response);
    }

    // Lấy theo ngày tháng năm
    @GetMapping("/queue/date")
    public ResponseEntity<List<WaitingListResponseDTO>> getWaitingListForDate(@RequestParam LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<WaitingListEntity> waitingList = waitingListService.getWaitingListForDate(startOfDay, endOfDay);

        if (waitingList == null || waitingList.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<WaitingListResponseDTO> response = waitingList.stream().map(waitingListEntity -> {
            WaitingListResponseDTO dto = new WaitingListResponseDTO();
            dto.setWaitingListId(waitingListEntity.getWaitingListId());
            dto.setExpectedWaitingTime(waitingListEntity.getExpectedWaitingTime());

            // ✅ TÍNH TOÁN THỜI GIAN CÒN LẠI CHÍNH XÁC
            if (waitingListEntity.getExpectedWaitingTime() != null) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expectedTime = waitingListEntity.getExpectedWaitingTime();
                long seconds = Duration.between(now, expectedTime).getSeconds();
                if (seconds < 0) seconds = 0;
                dto.setRemainingSeconds(seconds);
                dto.setRemainingTimeFormatted(formatSeconds(seconds));
            }

            dto.setUserId(waitingListEntity.getUser().getUserID());
            dto.setChargingStationId(waitingListEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(waitingListEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(waitingListEntity.getCar().getCarID());
            dto.setOutedAt(waitingListEntity.getOutedAt());
            dto.setCreatedAt(waitingListEntity.getCreatedAt());
            dto.setStatus(waitingListEntity.getStatus());
            dto.setStationName(waitingListEntity.getChargingStation().getNameChargingStation());
            return dto;
        }).toList();
        return ResponseEntity.ok(response);
    }

    // Lấy hàng chờ theo WaitingListId
    @GetMapping("/queue/{waitingListId}")
    public ResponseEntity<WaitingListResponseDTO> getWaitingListById(@PathVariable String waitingListId) {
        WaitingListEntity waitingListEntity = waitingListService.getWaitingListForWaitingListId(waitingListId);

        if (waitingListEntity == null) {
            return ResponseEntity.notFound().build();
        }

        WaitingListResponseDTO dto = new WaitingListResponseDTO();
        dto.setWaitingListId(waitingListEntity.getWaitingListId());
        dto.setExpectedWaitingTime(waitingListEntity.getExpectedWaitingTime());

        // ✅ TÍNH TOÁN THỜI GIAN CÒN LẠI CHÍNH XÁC
        if (waitingListEntity.getExpectedWaitingTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expectedTime = waitingListEntity.getExpectedWaitingTime();
            long seconds = Duration.between(now, expectedTime).getSeconds();
            if (seconds < 0) seconds = 0;
            dto.setRemainingSeconds(seconds);
            dto.setRemainingTimeFormatted(formatSeconds(seconds));
        }

        dto.setUserId(waitingListEntity.getUser().getUserID());
        dto.setChargingStationId(waitingListEntity.getChargingStation().getIdChargingStation());
        dto.setChargingPostId(waitingListEntity.getChargingPost().getIdChargingPost());
        dto.setCarId(waitingListEntity.getCar().getTypeCar());
        dto.setOutedAt(waitingListEntity.getOutedAt());
        dto.setCreatedAt(waitingListEntity.getCreatedAt());
        dto.setStatus(waitingListEntity.getStatus());
        dto.setStationName(waitingListEntity.getChargingStation().getNameChargingStation());
        return ResponseEntity.ok(dto);
    }
    @GetMapping("/getWaitingTimeByPost/{postId}")
    public long getWaitingTimeByPost(@PathVariable String postId) {
        long time =0; // mặc định chưa biết
        ChargingSessionEntity session = chargingSessionService.getNewSessionInPostId(postId);
        if(session != null) {
            time = Duration.between(LocalDateTime.now(), session.getExpectedEndTime()).getSeconds();
        }
        return time;
    }
}
