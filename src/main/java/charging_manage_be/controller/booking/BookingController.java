package charging_manage_be.controller.booking;

import charging_manage_be.model.dto.booking.BookingRequestDTO;
import charging_manage_be.model.dto.booking.BookingResponseDTO;
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.services.booking.BookingService;
import charging_manage_be.services.status_service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserStatusService userStatusService;
    private final String STATUS_BOOKING = "booking";
    private final String STATUS_WAITING = "waiting";
    @PostMapping("/create")
    public ResponseEntity<?> processBooking(@RequestBody BookingRequestDTO booking) { // ? có nghĩa là có thể là Booking hoặc WaitingList
        int result = bookingService.handleBookingNavigation(booking.getUser(), booking.getChargingPost(), booking.getCar()); // Trả về một result có thể là Booking hoặc WaitingList
        if (result != -1) {
            userStatusService.setUserStatus(booking.getUser(), STATUS_WAITING);
        }else {
            userStatusService.setUserStatus(booking.getUser(), STATUS_BOOKING);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/complete/{bookingId}")
    public ResponseEntity<String> completeBooking(@PathVariable String bookingID) {
        BookingEntity completedBooking = bookingService.completeBooking(bookingID);
        if (completedBooking != null) {
            return ResponseEntity.ok("Booking completed successfully" );
        }
       else {
            return ResponseEntity.notFound().build();
       }
    }

    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<String> cancelBooking(@PathVariable String bookingID) {
        BookingEntity booking =  bookingService.cancelBooking(bookingID);
        if (booking != null) {
            userStatusService.idleUserStatus(booking.getUser().getUserID());
            return ResponseEntity.ok("Booking cancelled successfully");
        }
        else {
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/getByPost/{postId}")
    public ResponseEntity<List<BookingResponseDTO>> getByPost(@PathVariable String postId) {

        List<BookingResponseDTO> bookingResponseDTO = bookingService.getBookingByPostId(postId).stream().map(BookingEntity -> {
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setBookingId(BookingEntity.getBookingId());
            // Kiểm tra null trước khi lấy waitingListId
            if (BookingEntity.getWaitingList() == null) {
                dto.setWaitingListId(null);
            } else {
                dto.setWaitingListId(BookingEntity.getWaitingList().getWaitingListId());
            }
            dto.setUserId(BookingEntity.getUser().getUserID());
            dto.setChargingStationId(BookingEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(BookingEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(BookingEntity.getCar().getCarID());
            dto.setCreatedAt(BookingEntity.getCreatedAt());
            dto.setMaxWaitingTime(BookingEntity.getMaxWaitingTime());
            dto.setStatus(BookingEntity.getStatus());
            dto.setArrivalTime(BookingEntity.getArrivalTime());
            return dto;
        }).toList();
        return ResponseEntity.ok(bookingResponseDTO);
    }

    @GetMapping("/getByStation/{stationId}")
    public ResponseEntity<List<BookingResponseDTO>> getByStation(@PathVariable String stationId) {

        List<BookingResponseDTO> bookingResponseDTO = bookingService.getBookingByStationId(stationId).stream().map(BookingEntity -> {
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setBookingId(BookingEntity.getBookingId());
            if (BookingEntity.getWaitingList() == null) {
                dto.setWaitingListId(null);
            } else {
                dto.setWaitingListId(BookingEntity.getWaitingList().getWaitingListId());
            }
            dto.setUserId(BookingEntity.getUser().getUserID());
            dto.setChargingStationId(BookingEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(BookingEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(BookingEntity.getCar().getCarID());
            dto.setCreatedAt(BookingEntity.getCreatedAt());
            dto.setMaxWaitingTime(BookingEntity.getMaxWaitingTime());
            dto.setStatus(BookingEntity.getStatus());
            dto.setArrivalTime(BookingEntity.getArrivalTime());
            return dto;
        }).toList();
        return ResponseEntity.ok(bookingResponseDTO);
    }

    @GetMapping("/getByUser/{userId}")
    public  ResponseEntity<List<BookingResponseDTO>> getByUser(@PathVariable String userId) {

        List<BookingResponseDTO> bookingResponseDTO = bookingService.getBookingByUserId(userId).stream().map(BookingEntity -> {
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setBookingId(BookingEntity.getBookingId());
            if (BookingEntity.getWaitingList() == null) {
                dto.setWaitingListId(null);
            } else {
                dto.setWaitingListId(BookingEntity.getWaitingList().getWaitingListId());
            }
            dto.setUserId(BookingEntity.getUser().getUserID());
            dto.setChargingStationId(BookingEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(BookingEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(BookingEntity.getCar().getCarID());
            dto.setCreatedAt(BookingEntity.getCreatedAt());
            dto.setMaxWaitingTime(BookingEntity.getMaxWaitingTime());
            dto.setStatus(BookingEntity.getStatus());
            dto.setArrivalTime(BookingEntity.getArrivalTime());
            return dto;
        }).toList();
        return ResponseEntity.ok(bookingResponseDTO);
    }
    @GetMapping("/getByCreatedDate")
    public  ResponseEntity<List<BookingResponseDTO>> getByCreatedDate(@RequestParam LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<BookingResponseDTO> bookingResponseDTO = bookingService.getBookingByCreatedDate(startOfDay, endOfDay).stream().map(BookingEntity -> {
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setBookingId(BookingEntity.getBookingId());
            if (BookingEntity.getWaitingList() == null) {
                dto.setWaitingListId(null);
            } else {
                dto.setWaitingListId(BookingEntity.getWaitingList().getWaitingListId());
            }
            dto.setUserId(BookingEntity.getUser().getUserID());
            dto.setChargingStationId(BookingEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(BookingEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(BookingEntity.getCar().getCarID());
            dto.setCreatedAt(BookingEntity.getCreatedAt());
            dto.setMaxWaitingTime(BookingEntity.getMaxWaitingTime());
            dto.setStatus(BookingEntity.getStatus());
            dto.setArrivalTime(BookingEntity.getArrivalTime());
            return dto;
        }).toList();
        return ResponseEntity.ok(bookingResponseDTO);
    }

    @GetMapping("/getByWaitingListId/{waitingListId}")
    public   ResponseEntity<List<BookingResponseDTO>> getByWaitingListId(@PathVariable String waitingListId) {

        List<BookingResponseDTO> bookingResponseDTO = bookingService.getBookingByWaitingListId(waitingListId).stream().map(BookingEntity -> {
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setBookingId(BookingEntity.getBookingId());
            if (BookingEntity.getWaitingList() == null) {
                dto.setWaitingListId(null);
            } else {
                dto.setWaitingListId(BookingEntity.getWaitingList().getWaitingListId());
            }
            dto.setUserId(BookingEntity.getUser().getUserID());
            dto.setChargingStationId(BookingEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(BookingEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(BookingEntity.getCar().getCarID());
            dto.setCreatedAt(BookingEntity.getCreatedAt());
            dto.setMaxWaitingTime(BookingEntity.getMaxWaitingTime());
            dto.setStatus(BookingEntity.getStatus());
            dto.setArrivalTime(BookingEntity.getArrivalTime());
            return dto;
        }).toList();
        return ResponseEntity.ok(bookingResponseDTO);
    }

    @GetMapping("/getByBookingId/{bookingId}")
    public   ResponseEntity<BookingResponseDTO> getByBookingId(@PathVariable String bookingId) {
            BookingEntity BookingEntity = bookingService.getBookingByBookingId(bookingId);
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setBookingId(BookingEntity.getBookingId());
            if (BookingEntity.getWaitingList() == null) {
                dto.setWaitingListId(null);
            } else {
                dto.setWaitingListId(BookingEntity.getWaitingList().getWaitingListId());
            }
            dto.setUserId(BookingEntity.getUser().getUserID());
            dto.setChargingStationId(BookingEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(BookingEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(BookingEntity.getCar().getCarID());
            dto.setCreatedAt(BookingEntity.getCreatedAt());
            dto.setMaxWaitingTime(BookingEntity.getMaxWaitingTime());
            dto.setStatus(BookingEntity.getStatus());
            dto.setArrivalTime(BookingEntity.getArrivalTime());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/getByStatus/{statusList}")
    public ResponseEntity<List<BookingResponseDTO>> getByStatus(@PathVariable String statusList) {
        List<BookingResponseDTO> bookingResponseDTO = bookingService.getBookingByStatus(statusList).stream().map(BookingEntity -> {
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setBookingId(BookingEntity.getBookingId());
            if (BookingEntity.getWaitingList() == null) {
                dto.setWaitingListId(null);
            } else {
                dto.setWaitingListId(BookingEntity.getWaitingList().getWaitingListId());
            }
            dto.setUserId(BookingEntity.getUser().getUserID());
            dto.setChargingStationId(BookingEntity.getChargingStation().getIdChargingStation());
            dto.setChargingPostId(BookingEntity.getChargingPost().getIdChargingPost());
            dto.setCarId(BookingEntity.getCar().getCarID());
            dto.setCreatedAt(BookingEntity.getCreatedAt());
            dto.setMaxWaitingTime(BookingEntity.getMaxWaitingTime());
            dto.setStatus(BookingEntity.getStatus());
            dto.setArrivalTime(BookingEntity.getArrivalTime());
            return dto;
        }).toList();
        return ResponseEntity.ok(bookingResponseDTO);
    }


//    @PostMapping("/process/{chargingPostID}") // này không cần thiết vì sẽ tự động xử lý khi hoàn thành hoặc hủy booking
//    public ResponseEntity<String> processNextBooking(@PathVariable String chargingPostID) {
//        BookingEntity processedBooking = bookingService.processBooking(chargingPostID);
//        if (processedBooking != null) {
//            return ResponseEntity.ok("Processed next booking successfully");
//        } else {
//            return ResponseEntity.ok("No pending bookings to process");
//        }
//    }
}
