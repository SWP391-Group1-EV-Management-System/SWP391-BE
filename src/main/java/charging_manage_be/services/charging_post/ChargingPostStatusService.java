package charging_manage_be.services.charging_post;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.repository.charging_post.ChargingPostRepository;
import charging_manage_be.repository.charging_session.ChargingSessionRepository;
import charging_manage_be.repository.waiting_list.WaitingListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChargingPostStatusService {

    private final BookingRepository bookingRepository;
    private final ChargingSessionRepository chargingSessionRepository;
    private final WaitingListRepository waitingListRepository;
    private final ChargingPostRepository chargingPostRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Broadcast trạng thái của một trụ sạc đến tất cả client đang lắng nghe
     * FE subscribe: /topic/post/{postId}/status
     */
    public void broadcastPostStatus(String postId) {
        Map<String, Object> status = getPostStatus(postId);

        System.out.println("🔔 [ChargingPostStatus] Broadcasting status for post: " + postId);
        System.out.println("📊 [ChargingPostStatus] Status: " + status);

        // Gửi đến topic công khai cho tất cả client (UI trụ ảo)
        simpMessagingTemplate.convertAndSend(
            "/topic/post/" + postId + "/status",
            status
        );
    }

    /**
     * Lấy trạng thái chi tiết của một trụ sạc
     * ✅ Ưu tiên: Session (đang sạc) > Booking (đã đặt) > Available (rảnh)
     */
    public Map<String, Object> getPostStatus(String postId) {
        Map<String, Object> status = new HashMap<>();

        // 1. ✅ ƯU TIÊN CAO NHẤT: Kiểm tra có session đang chạy không (isDone=false)
        var post = chargingPostRepository.findById(postId).orElse(null);
        List<ChargingSessionEntity> activeSessions = post != null ?
            chargingSessionRepository.findTopByChargingPostAndIsDoneOrderByStartTimeDesc(post, false)
                .stream().toList() : List.of();

        // 2. Kiểm tra có booking không (CONFIRMED hoặc CHARGING)
        List<BookingEntity> activeBookings = bookingRepository
            .findFirstByChargingPost_IdChargingPostAndStatusInOrderByCreatedAtAsc(
                postId,
                List.of("CONFIRMED", "CHARGING")
            ).stream().toList();

        // 3. Đếm số người trong hàng đợi
        long waitingCount = waitingListRepository.countByChargingPost_IdChargingPostAndStatus(postId, "WAITING");

        // 4. Xác định trạng thái chính (theo thứ tự ưu tiên)
        String mainStatus;
        String subStatus = null;
        Map<String, Object> details = new HashMap<>();

        // ✅ CASE 1: Có session đang chạy (isDone=false) → Trạng thái quan trọng nhất
        if (!activeSessions.isEmpty()) {
            ChargingSessionEntity session = activeSessions.get(0);
            mainStatus = "CHARGING";  // ✅ Gửi trạng thái CHARGING khi có session đang chạy
            subStatus = "IN_PROGRESS";
            details.put("sessionId", session.getChargingSessionId());
            details.put("userId", session.getUser().getUserID());
            details.put("userName", session.getUser().getFirstName() + " " + session.getUser().getLastName());
            details.put("startTime", session.getStartTime());
            details.put("expectedEndTime", session.getExpectedEndTime());

            // Nếu session có booking thì thêm thông tin booking
            if (session.getBooking() != null) {
                details.put("bookingId", session.getBooking().getBookingId());
            }
        }
        // ✅ CASE 2: Có booking nhưng chưa có session (đã đặt, chờ đến)
        else if (!activeBookings.isEmpty()) {
            BookingEntity booking = activeBookings.get(0);
            mainStatus = "BOOKED";
            details.put("bookingId", booking.getBookingId());
            details.put("userId", booking.getUser().getUserID());
            details.put("userName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
            details.put("bookingStatus", booking.getStatus());
            details.put("createdAt", booking.getCreatedAt());

            if (booking.getStatus().equals("CHARGING")) {
                subStatus = "CHARGING";
            } else if (booking.getStatus().equals("CONFIRMED")) {
                subStatus = "WAITING_FOR_ARRIVAL";
            }
        }
        // ✅ CASE 3: Không có session, không có booking nhưng có người đang chờ → Trạng thái WAITING
        else if (waitingCount > 0) {
            mainStatus = "WAITING";
            subStatus = "HAS_QUEUE";
            details.put("message", "Có " + waitingCount + " người đang chờ");
            details.put("queueCount", waitingCount);
        }
        // ✅ CASE 4: Không có gì cả → Trụ rảnh
        else {
            mainStatus = "AVAILABLE";
        }

        status.put("postId", postId);
        status.put("status", mainStatus);
        status.put("subStatus", subStatus);
        status.put("waitingCount", waitingCount);
        status.put("details", details);
        status.put("timestamp", java.time.LocalDateTime.now());

        System.out.println("📊 [getPostStatus] Post: " + postId + " | Status: " + mainStatus + " | Sessions: " + activeSessions.size() + " | Bookings: " + activeBookings.size() + " | Waiting: " + waitingCount);

        return status;
    }

    /**
     * Broadcast trạng thái của tất cả các trụ trong một trạm
     */
    public void broadcastStationStatus(String stationId) {
        var posts = chargingPostRepository.findByChargingStation_IdChargingStation(stationId);

        System.out.println("🔔 [ChargingPostStatus] Broadcasting status for station: " + stationId);

        for (var post : posts) {
            broadcastPostStatus(post.getIdChargingPost());
        }
    }
}

