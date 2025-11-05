package charging_manage_be.controller.charging;

import charging_manage_be.services.charging_post.ChargingPostStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/charging-post")
@RequiredArgsConstructor
public class ChargingPostStatusController {

    private final ChargingPostStatusService chargingPostStatusService;

    @GetMapping("/{postId}/status")
    public ResponseEntity<Map<String, Object>> getPostStatus(@PathVariable String postId) {
        Map<String, Object> status = chargingPostStatusService.getPostStatus(postId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/{postId}/broadcast")
    public ResponseEntity<String> broadcastPostStatus(@PathVariable String postId) {
        chargingPostStatusService.broadcastPostStatus(postId);
        return ResponseEntity.ok("Broadcasted status for post: " + postId);
    }
    @GetMapping("/station/{stationId}/broadcast")
    public ResponseEntity<String> broadcastStationStatus(@PathVariable String stationId) {
        chargingPostStatusService.broadcastStationStatus(stationId);
        return ResponseEntity.ok("Broadcasted status for all posts in station: " + stationId);
    }
}

