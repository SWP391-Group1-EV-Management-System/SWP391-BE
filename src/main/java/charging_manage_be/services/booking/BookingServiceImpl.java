package charging_manage_be.services.booking;

import charging_manage_be.model.dto.booking.BookingIdForSessionResDTO;
import charging_manage_be.model.dto.booking.BookingResponseDTO;
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.reputations.ReputationLevelEntity;
import charging_manage_be.model.entity.reputations.UserReputationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.repository.cars.CarRepository;
import charging_manage_be.repository.charging_post.ChargingPostRepository;
import charging_manage_be.repository.charging_session.ChargingSessionRepository;
import charging_manage_be.repository.charging_station.ChargingStationRepository;
import charging_manage_be.repository.reputations.ReputationLevelRepository;
import charging_manage_be.repository.user_reputations.UserReputationRepository;
import charging_manage_be.repository.users.UserRepository;
import charging_manage_be.repository.waiting_list.WaitingListRepository;
import charging_manage_be.services.charging_post.ChargingPostService;
import charging_manage_be.services.charging_post.ChargingPostStatusService;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.status_service.UserStatusService;
import charging_manage_be.services.waiting_list.WaitingListService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
@RequiredArgsConstructor // Tự động tạo constructor với tất cả các trường final
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final WaitingListRepository waitingListRepository;
    @Lazy
    private final ChargingSessionRepository sessionRepo;
    private final WaitingListService waitingListService;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final ChargingPostRepository chargingPostRepository;
    private final ReputationLevelRepository reputationLevelRepository;
    private final UserReputationRepository userReputationRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private static final String KEY_QUEUE_POST = "queue:post:";
    private final String STATUS_BOOKING = "booking";
    private final UserStatusService userStatusService;
    @Lazy
    private final ChargingPostStatusService chargingPostStatusService;
    private final int characterLength = 5;
    private final int numberLength = 5;

    private String redisKey(String chargingPostId) { // redisKey để tạo key cho danh sách chờ trong Redis
        return KEY_QUEUE_POST + chargingPostId; // gán tên post thành biến final dễ chỉnh sửa nếu cần
    }

    private String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (bookingRepository.existsById(newId));
        return newId;
    }

    /*
    Trong thực tế, ở tầng controller hoặc API, người ta thường nhận các tham số đơn giản như userID, postID, carId (hoặc một DTO chứa các trường này). Sau đó, ở tầng service, các tham số này sẽ được dùng để truy vấn và tạo ra entity (WaitingListEntity) trước khi xử lý logic.

    Không nên truyền thẳng entity từ UI/controller xuống service, vì:


    Entity thường gắn với database, không phù hợp để expose ra ngoài.
    Dễ gây lỗi bảo mật, khó kiểm soát dữ liệu đầu vào.
    Tóm lại:
    Controller nhận DTO hoặc các trường đơn giản (userID, postID, carId).
    Service nhận DTO hoặc các trường này, tự tạo entity từ DB.
    Không truyền entity trực tiếp từ UI/controller.
     */
    // sửa tham số truyền vào đơn giản
    @Transactional
    public int handleBookingNavigation(String userId, String chargingPostId, String carId) {
        if(isUserBooking(userId) || waitingListService.isUserWaiting(userId))
        {
            return -2;
        }else {
            // Check trạng thái hiện tại của trạm sạc
            // push vào redis để tránh race condition
            redisTemplate.opsForList().rightPush(redisKey(chargingPostId), userId);
            int positionInQueue;
            Optional<BookingEntity> latestStatusChargingPost = bookingRepository
                    .findFirstByChargingPost_IdChargingPostAndStatusInOrderByCreatedAtAsc(chargingPostId, List.of("CONFIRMED", "CHARGING"));
            ChargingPostEntity postE = chargingPostRepository.findById(chargingPostId).orElse(null);
            Optional<ChargingSessionEntity> latestStatusChargingPostInSession = sessionRepo.findTopByChargingPostAndIsDoneOrderByStartTimeDesc(postE, false);
            // thêm xử lý khi có người đang trong session người dùng cũng phải vào waiting và chờ session end
            // Câu lệnh trên sẽ lấy trạng thái mới nhất của trạm sạc với ID trụ sạc là chargingPostID
            // Và trạng thái của booking là "waiting" hoặc "charging"
            // Status là status của trụ ở trong bảng booking chứ không phải trong bảng chargingPost

            // Nếu trạng thái hiện tại của trạm sạc là "waiting" hoặc "charging" thì sẽ vào danh sách chờ bởi vì trạm sạc đang có người dùng

            if (latestStatusChargingPost.isEmpty() && latestStatusChargingPostInSession.isEmpty()) { // Nếu trạm sạc hiện tại không có trạng thái "waiting" hoặc "charging" thì sẽ tạo booking luôn
                // Xoá userId khỏi danh sách chờ trong Redis vì user sắp được tạo booking

                redisTemplate.opsForList().remove(redisKey(chargingPostId), 1, userId);
                BookingEntity bookingEntity = new BookingEntity();
                bookingEntity.setBookingId(generateUniqueId());

                // fetch lại user từ DB để chắc chắn có userReputations
                UserEntity user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // fetch car, chargingStation, post từ DB giống user
                CarEntity car = carRepository.findById(carId)
                        .orElseThrow(() -> new RuntimeException("Car not found"));

                ChargingPostEntity post = chargingPostRepository.findById(chargingPostId)
                        .orElseThrow(() -> new RuntimeException("Post not found"));
                ChargingStationEntity station = chargingStationRepository.findStationByChargingPostEntity(chargingPostId)
                        .orElseThrow(() -> new RuntimeException("Station not found"));

                // Phải lấy được độ uy tín hiện tại của user, sau đó xét trong danh sách các mức độ uy tín (reputation levels) để lấy maxWaitingTime tương ứng
                UserReputationEntity userReputation = userReputationRepository.findFirstByUser_UserIDOrderByCreatedAtDesc(userId).orElseThrow();
                bookingEntity.setMaxWaitingTime(userReputation.getReputationLevel().getMaxWaitMinutes());
                bookingEntity.setUser(user);
                bookingEntity.setChargingStation(station);
                bookingEntity.setChargingPost(post);
                bookingEntity.setCar(car);
                bookingEntity.setCreatedAt(LocalDateTime.now());

                // Lấy maxWaitingTime từ bảng user_reputation
                // Lấy tất cả các record trong bảng user_reputation của user, sau đó lấy max của maxWaitMinutes
                // Nếu không có record nào trong bảng user_reputation thì sẽ ném ra ngoại lệ
//                bookingEntity.setMaxWaitingTime(user.getUserReputations()
//                        .stream().mapToInt(reputation -> reputation.getReputationLevel().getMaxWaitMinutes())
//                        .max().orElseThrow(() -> new RuntimeException("No reputation levels found for user")));


                bookingEntity.setStatus("CONFIRMED");
                BookingEntity savedBooking = bookingRepository.save(bookingEntity);

                simpMessagingTemplate.convertAndSendToUser(userId,
                        "/queue/notifications/" + chargingPostId,
                        "User " + userId + " booked successfully");

                // ✅ THÊM: Broadcast trạng thái trụ đã được booking
                chargingPostStatusService.broadcastPostStatus(chargingPostId);

                return -1; // Trả về -1 để báo rằng không vào danh sách chờ mà đã tạo booking luôn


            } else {
                // Khi có các trạng thái "waiting" hoặc "charging" thì sẽ vào danh sách chờ
                // Lưu record vào bảng waitingList
                // WaitingListEntity savedWaiting = waitingListService.addToWaitingList(waitingRequest);
                /*
                Bạn nên forward các tham số đơn giản như userId, postId, carId sang service của waiting list để service đó tự xử lý
                việc tạo WaitingListEntity. Điều này giúp tách biệt logic, dễ bảo trì, kiểm soát dữ liệu đầu vào tốt hơn
                và tuân thủ nguyên tắc phân tầng trong Spring Boot. Không nên tạo entity trực tiếp ở service booking
                 */
                waitingListService.addToWaitingList(userId, chargingPostId, carId);
                // Thêm userID vào danh sách chờ trong Redis
//            redisTemplate.opsForList().rightPush(redisKey(chargingPostID), waitingRequest.getUser().getUserID());

                // Trả về thông báo tới user rằng đã vào danh sách chờ
                // Nếu muốn gửi thông báo đến đích danh người dùng thì sẽ gửi đến kênh "/user/{userID}/queue/notifications"
                // Muốn lấy số thứ tự của user trong danh sách chờ thì sẽ lấy vị trí của userID trong danh sách Redis
                positionInQueue = redisTemplate.opsForList().range(redisKey(chargingPostId), 0, -1).indexOf(userId) + 1;
                //simpMessagingTemplate.convertAndSendToUser(userId, "/queue/notifications",
                //        "Your position in queue: " + positionInQueue);

                // ✅ THÊM: Broadcast trạng thái trụ có người mới vào hàng chờ
                chargingPostStatusService.broadcastPostStatus(chargingPostId);

                return positionInQueue; // số thứ tự chờ
            }
        }
    }


    @Override
    @Transactional
    public BookingEntity completeBooking(String bookingID) {
        BookingEntity booking = bookingRepository.findById(bookingID).orElseThrow();

        booking.setStatus("COMPLETED");
        // bỏ vì đã thay đổi cột này thành trạng thái đến trụ
        //booking.setArrivalTime(LocalDateTime.now());

        // Gửi thông báo đến user rằng booking đã hoàn thành
        simpMessagingTemplate.convertAndSendToUser(booking.getUser().getUserID(), "/queue/notifications",
                "Your Booking: " + booking.getBookingId() + " completed successfully");

        BookingEntity savedBooking = bookingRepository.save(booking);

        // ✅ THÊM: Broadcast trạng thái trụ đã hoàn thành (trụ rảnh)
        chargingPostStatusService.broadcastPostStatus(booking.getChargingPost().getIdChargingPost());

        // tự động thay thế bằng một booking mới từ waiting chờ nếu có
        // phải set nếu end time bên session bằng với thời gian expected end bên session thì mới gọi hàm này
//        boolean isWaitingDriver = waitingListRepository
//                .findFirstByChargingPost_IdChargingPostAndStatusOrderByCreatedAtAsc(
//                        booking.getChargingPost().getIdChargingPost(), "WAITING")
//                .isPresent();
//        if (isWaitingDriver) {
//            processBooking(booking.getChargingPost().getIdChargingPost());
//        }
        return savedBooking;
    }

    @Override
    @Transactional
    public BookingEntity cancelBooking(String bookingID) {
        BookingEntity booking = bookingRepository.findById(bookingID).orElseThrow();

        booking.setStatus("CANCELLED");

        // Gửi thông báo đến user rằng booking đã bị hủy
        simpMessagingTemplate.convertAndSendToUser(booking.getUser().getUserID(), "/queue/notifications",
                "Your Booking: " + booking.getBookingId() + " cancel booking successfully");

        BookingEntity savedBooking = bookingRepository.save(booking);

        // ✅ THÊM: Broadcast trạng thái trụ đã bị cancel (trụ rảnh)
        chargingPostStatusService.broadcastPostStatus(booking.getChargingPost().getIdChargingPost());

        // theo flow mới ( khi chưa đủ giờ) phải hỏi driver trong waiting rằng có muốn sạc luôn hay không, nếu đồng ý thì chuyển từ waiting ra
        // không muôn sạc luôn thì phải chờ đến giờ
        // khi đã đủ giờ tự động driver trong hàng đợi sẽ được lấy ra
//        boolean isWaitingDriver = waitingListRepository
//                .findFirstByChargingPost_IdChargingPostAndStatusOrderByCreatedAtAsc(
//                        booking.getChargingPost().getIdChargingPost(), "WAITING")
//                .isPresent();
//        if (isWaitingDriver) {
//            processBooking(booking.getChargingPost().getIdChargingPost());
//        }
        return savedBooking;
    }


    @Transactional
    public BookingEntity processBooking(String chargingPostId) {
        BookingEntity saved = new BookingEntity();
        // Tìm người đầu tiên trong danh sách chờ theo trụ sạc và trạng thái "WAITING"
        boolean isWaitingDriver = waitingListRepository
                .findFirstByChargingPost_IdChargingPostAndStatusOrderByCreatedAtAsc(
                        chargingPostId, "WAITING")
                .isPresent();
        if (isWaitingDriver){

        // Kiểm tra trạng thái hiện tại của trạm sạc, để nếu trụ có trạng thái "CONFIRMED" hoặc "CHARGING" thì trả về ngoại lệ và không tạo booking mới
        boolean busy = bookingRepository
                .findFirstByChargingPost_IdChargingPostAndStatusInOrderByCreatedAtAsc(
                        chargingPostId, List.of("CONFIRMED", "CHARGING"))
                .isPresent();// "CONFIRMED", "CHARGING", "COMPLETE", "CANCEL"
        if (busy) {
            throw new IllegalStateException("Charging post " + chargingPostId + " is still busy");
        }


        // Nếu trạm sạc không bận, thì tạo booking mới từ người đầu tiên trong danh sách chờ theo trụ sạc và có trạng thái "WAITING"
        WaitingListEntity waitingList = waitingListRepository
                .findFirstByChargingPost_IdChargingPostAndStatusOrderByCreatedAtAsc(
                        chargingPostId, "WAITING")
                .orElseThrow(() -> new RuntimeException("No WAITING record for post " + chargingPostId));


        // Tạo booking mới cho người từ danh sách chờ vừa lấy được
        BookingEntity booking = new BookingEntity();
        booking.setBookingId(generateUniqueId());
        booking.setWaitingList(waitingList);
        booking.setUser(waitingList.getUser());
        booking.setChargingStation(waitingList.getChargingStation());
        booking.setChargingPost(waitingList.getChargingPost());
        booking.setCar(waitingList.getCar());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setMaxWaitingTime(
                waitingList.getUser().getUserReputations().stream()
                        .mapToInt(r -> r.getReputationLevel().getMaxWaitMinutes())
                        .max().orElse(0)
        );
        booking.setStatus("CONFIRMED");
        saved = bookingRepository.save(booking);

        waitingList.setStatus("COMPLETED");
        waitingList.setOutedAt(LocalDateTime.now());
        waitingListRepository.save(waitingList);

        // Xóa user khỏi Redis queue
        redisTemplate.opsForList().leftPop(redisKey(chargingPostId));
        userStatusService.setUserStatus(booking.getUser().getUserID(), STATUS_BOOKING);

        // Gửi thoogn báo tới user rằng booking đã được xác nhận
        simpMessagingTemplate.convertAndSendToUser(
                waitingList.getUser().getUserID(),
                "/queue/booking-status",
                java.util.Map.of(
                    "status", "CONFIRMED",
                    "bookingId", saved.getBookingId(),
                    "message", "Your booking has been confirmed",
                    "postId", chargingPostId
                )
        );

        // Cập nhật lại vị trí trong hàng đợi của các user còn lại trong Redis và gửi notification về vị trí mới
        List<String> remainingUsersInRedis = redisTemplate.opsForList().range(redisKey(chargingPostId), 0, -1);
        if (remainingUsersInRedis != null && !remainingUsersInRedis.isEmpty()) {
            for (int i = 0; i < remainingUsersInRedis.size(); i++) {
                String userId = remainingUsersInRedis.get(i);
                int newPosition = i + 1;

                // Gửi notification về vị trí mới
                simpMessagingTemplate.convertAndSendToUser(
                        userId,
                        "/queue/position-update",
                        java.util.Map.of(
                            "position", newPosition,
                            "postId", chargingPostId,
                            "message", "Your position has been updated to " + newPosition
                        )
                );
            }
        }
        }
        return saved;
    }
    @Override
    @Transactional
    public boolean updateChargingBookingStatus(String bookingId) {


        BookingEntity booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return false; // Booking không tồn tại
        }
        booking.setStatus("CHARGING");
        booking.setArrivalTime(LocalDateTime.now());
        bookingRepository.save(booking);
        return true;
    }

    @Override
    public List<BookingEntity> getBookingByPostId(String postId) {
        ChargingPostEntity chargingPost = chargingPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return bookingRepository.findByChargingPost(chargingPost);
    }

    @Override
    public List<BookingEntity> getBookingByStationId(String stationId) {
        ChargingStationEntity chargingStation = chargingStationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return bookingRepository.findByChargingStation(chargingStation);
    }

    @Override
    public List<BookingEntity> getBookingByUserId(String userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepository.findByUser(user);
    }

    @Override
    public List<BookingEntity> getBookingByCreatedDate(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return bookingRepository.findByCreatedAtBetween(startOfDay, endOfDay);

    }

    @Override
    public BookingEntity getBookingByWaitingListId(String waitingListId) {
        WaitingListEntity waitingList = waitingListRepository.findById(waitingListId)
                .orElseThrow(() -> new RuntimeException("Waiting list not found"));
        return bookingRepository.findByWaitingList(waitingList);
    }

    @Override
    public BookingEntity getBookingByBookingId(String bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }

    @Override
    public List<BookingEntity> getBookingByStatus(String status) {
        return bookingRepository.findByStatusIn(List.of(status));
    }

    @Override
    public List<BookingEntity> getExpiredBookings(LocalDateTime currentTime) {
        return bookingRepository.findExpiredBookings().stream()
                .filter(booking -> booking.getCreatedAt().plusMinutes(booking.getMaxWaitingTime()).isBefore(currentTime) ||
                         booking.getCreatedAt().plusMinutes(booking.getMaxWaitingTime()).isEqual(currentTime)).collect(Collectors.toList());
    }

    @Override
    public BookingIdForSessionResDTO getLatestConfirmedBookingByUserId(String userId) {
        BookingEntity booking = bookingRepository.findFirstByUser_UserIDAndStatusOrderByCreatedAtDesc(userId, "CONFIRMED");
        if (booking == null) {
            return null;
        }
        BookingIdForSessionResDTO bookingIdForSessionResDTO = new BookingIdForSessionResDTO();
        bookingIdForSessionResDTO.setBookingId(booking.getBookingId());
        return bookingIdForSessionResDTO;
    }

    @Override
    public boolean isUserBooking(String userId) {
        boolean booking = false;
        BookingEntity bookingCheck = bookingRepository.findFirstByUser_UserIDAndStatusOrderByCreatedAtDesc(userId, "CONFIRMED");
        BookingEntity bookingCheck2 = bookingRepository.findFirstByUser_UserIDAndStatusOrderByCreatedAtDesc(userId, "CHARGING");
        if(bookingCheck != null || bookingCheck2 != null) {
            booking = true;
        }
        return booking;
    }

    @Override
    public boolean isPostIdleInBooking(String postId) {
        ChargingPostEntity chargingPost = chargingPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        BookingEntity booking = bookingRepository.findFirstByChargingPost_IdChargingPostAndStatusInOrderByCreatedAtAsc(postId, List.of("CONFIRMED")).orElse(null);
        if(booking != null) {
            return  false; // bận
        }
        return true; //
    }

    @Override
    public String getBookingIdNewByUserId(String userId) {
        return bookingRepository.findFirstByUser_UserIDAndStatusOrderByCreatedAtDesc(userId, "CONFIRMED").getBookingId();
    }

    @Override
    public String getPostIdByNewBookingOfUserId(String userId) {
        BookingEntity book =  bookingRepository.findFirstByUser_UserIDAndStatusOrderByCreatedAtDesc(userId, "CONFIRMED");
        if(book != null) {
            return book.getChargingPost().getIdChargingPost();
        }
        return "null";
    }

    @Override
    public BookingEntity getNewBookingByUserId(String userId) {
        return bookingRepository.findFirstByUser_UserIDAndStatusOrderByCreatedAtDesc(userId, "CONFIRMED");
    }
}

