package charging_manage_be.controller.booking;

import charging_manage_be.model.dto.booking.WaitingListResponseDTO;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.services.booking.BookingService;
import charging_manage_be.services.charging_post.ChargingPostService;
import charging_manage_be.services.charging_station.ChargingStationService;
import charging_manage_be.services.waiting_list.WaitingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/waiting-list")
@RequiredArgsConstructor

public class WaitingListController {
    private final WaitingListService waitingListService;
    private final BookingService bookingService;
    private final ChargingPostService chargingPostService;

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

    @GetMapping("/queue/post/{chargingPostId}")
    public ResponseEntity<List<WaitingListResponseDTO>> getWaitingListForPost(@PathVariable String chargingPostId) {
        List<WaitingListResponseDTO> waitingList = waitingListService.getWaitingListForPost(chargingPostId).stream().map(waitingListEntity -> {
            WaitingListResponseDTO dto = new WaitingListResponseDTO();
            dto.setWaitingListId(waitingListEntity.getWaitingListId());
            dto.setExpectedWaitingTime(waitingListEntity.getExpectedWaitingTime());
            dto.setUserId(waitingListEntity.getUser().getUserID());
            dto.setChargingStationId(waitingListEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(waitingListEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(waitingListEntity.getCar().getCarID());
            dto.setOutedAt(waitingListEntity.getOutedAt());
            dto.setCreatedAt(waitingListEntity.getCreatedAt());
            dto.setStatus(waitingListEntity.getStatus());
            return dto;
        }).toList();
        return ResponseEntity.ok(waitingList);
    }
    // Cho thêm path để lấy  hàn chờ theo trạm, lấy theo ngày tháng năm, lấy theo userID

    //Lấy theo trạm
    @GetMapping("/queue/station/{chargingStationId}")
    public ResponseEntity<List<WaitingListResponseDTO>> getWaitingListForStation(@PathVariable String chargingStationId) {
        List<WaitingListResponseDTO> waitingList = waitingListService.getWaitingListForStation(chargingStationId).stream().map(waitingListEntity -> {
            ;
            WaitingListResponseDTO dto = new WaitingListResponseDTO();
            dto.setWaitingListId(waitingListEntity.getWaitingListId());
            dto.setExpectedWaitingTime(waitingListEntity.getExpectedWaitingTime());
            dto.setUserId(waitingListEntity.getUser().getUserID());
            dto.setChargingStationId(waitingListEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(waitingListEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(waitingListEntity.getCar().getCarID());
            dto.setOutedAt(waitingListEntity.getOutedAt());
            dto.setCreatedAt(waitingListEntity.getCreatedAt());
            dto.setStatus(waitingListEntity.getStatus());
            return dto;
        }).toList();
        return ResponseEntity.ok(waitingList);
    }

    // Lấy theo userID
    @GetMapping("/queue/users/{userId}")
    public ResponseEntity<List<WaitingListResponseDTO>> getWaitingListForUser(@PathVariable String userId) {
        List<WaitingListResponseDTO> waitingList = waitingListService.getWaitingListForUser(userId).stream().map(waitingListEntity -> {
            WaitingListResponseDTO dto = new WaitingListResponseDTO();
            dto.setWaitingListId(waitingListEntity.getWaitingListId());
            dto.setExpectedWaitingTime(waitingListEntity.getExpectedWaitingTime());
            dto.setUserId(waitingListEntity.getUser().getUserID());
            dto.setChargingStationId(waitingListEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(waitingListEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(waitingListEntity.getCar().getCarID());
            dto.setOutedAt(waitingListEntity.getOutedAt());
            dto.setCreatedAt(waitingListEntity.getCreatedAt());
            dto.setStatus(waitingListEntity.getStatus());
            return dto;
        }).toList();
        return ResponseEntity.ok(waitingList);
    }

    // Lấy theo ngày tháng năm
    @GetMapping("/queue/date")
    public ResponseEntity<List<WaitingListResponseDTO>> getWaitingListForDate(@RequestParam LocalDate date) {

        //LocalDateTime dateTime = LocalDate.parse(date).atStartOfDay(); // startOfDay là lấy khoảng thời gian từ 00:00:00 đến 23:59:59 của ngày đó
        // Khi này, dateTime sẽ là 2023-10-10T00:00:00 và ta sẽ lấy tất cả các record có createdAt trong khoảng từ 2023-10-10T00:00:00 đến 2023-10-10T23:59:59

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<WaitingListResponseDTO> waitingList = waitingListService.getWaitingListForDate(startOfDay, endOfDay).stream().map(waitingListEntity -> {
            WaitingListResponseDTO dto = new WaitingListResponseDTO();
            dto.setWaitingListId(waitingListEntity.getWaitingListId());
            dto.setExpectedWaitingTime(waitingListEntity.getExpectedWaitingTime());
            dto.setUserId(waitingListEntity.getUser().getUserID());
            dto.setChargingStationId(waitingListEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(waitingListEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(waitingListEntity.getCar().getCarID());
            dto.setOutedAt(waitingListEntity.getOutedAt());
            dto.setCreatedAt(waitingListEntity.getCreatedAt());
            dto.setStatus(waitingListEntity.getStatus());
            return dto;
        }).toList();
        return ResponseEntity.ok(waitingList);
    }

    // Lấy hàng chờ theo WaitingListId
    @GetMapping("/queue/{waitingListId}")
    public ResponseEntity<WaitingListResponseDTO> getWaitingListById(@PathVariable String waitingListId) {
        WaitingListEntity waitingListEntity = waitingListService.getWaitingListForWaitingListId(waitingListId);
        WaitingListResponseDTO dto = new WaitingListResponseDTO();
        dto.setWaitingListId(waitingListEntity.getWaitingListId());
        dto.setExpectedWaitingTime(waitingListEntity.getExpectedWaitingTime());
        dto.setUserId(waitingListEntity.getUser().getUserID());
        dto.setChargingStationId(waitingListEntity.getChargingStation().getIdChargingStation());
        dto.setChargingPostId(waitingListEntity.getChargingPost().getIdChargingPost());
        dto.setCarId(waitingListEntity.getCar().getCarID());
        dto.setOutedAt(waitingListEntity.getOutedAt());
        dto.setCreatedAt(waitingListEntity.getCreatedAt());
        dto.setStatus(waitingListEntity.getStatus());
        return ResponseEntity.ok(dto);
    }
}
