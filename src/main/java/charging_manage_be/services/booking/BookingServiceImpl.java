package charging_manage_be.services.booking;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
<<<<<<< HEAD
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.repository.waiting_list.WaitingListRepository;
import charging_manage_be.services.waiting_list.WaitingListServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingServiceImpl {
    @Autowired
    private BookingRepository bookingRepository;
    private WaitingListServiceImpl waitingListRepository;
    public boolean createBooking(BookingEntity booking) {
        String PostID = booking.getChargingPost().getIdChargingPost();
        boolean isAvailable = bookingRepository.isChargingPostAvailable(PostID);
        if(!isAvailable) {
            //gọi add hàng đợi
            waitingListRepository.handleWaitingListFromBooking(booking);
            return false; // báo cho người dùng
        }
        bookingRepository.save(booking);
        return true;
    }

=======
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.repository.cars.CarRepository;
import charging_manage_be.repository.charnging_post.ChargingPostRepository;
import charging_manage_be.repository.users.UserRepository;
import charging_manage_be.repository.waiting_list.WaitingListRepository;
import charging_manage_be.services.waiting_list.WaitingListService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{
    private final BookingRepository bookingRepository;
    private final WaitingListRepository waitingListRepository;
    private final WaitingListService waitingListService;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final ChargingPostRepository chargingPostRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;

    private String alphabet;
    private String integer;

    private String redisKey(String chargingPostId) { // redisKey để tạo key cho danh sách chờ trong Redis
        return "queue:post:" + chargingPostId;
    }

    private String randomIdMethod(){
        // Tạo ID ngẫu nhiên cho booking với 4 ký tư tự chữ và 4 ký tự số
        alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        integer = "0123456789";
        StringBuilder idBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int indexAlphabet = (int) (alphabet.length() * Math.random()); // ép kiểu int vì Math.random() trả về double và lấy theo độ dài của chuỗi alphabet
            idBuilder.append(alphabet.charAt(indexAlphabet)); // Và nối ký tự vào idBuilder
        }
        // Đây chỉ là lấy ký tự ngẫu nhiên từ chuỗi alphabet, không đảm bảo không trùng lặp
        // Bây giờ ta có 4 ký tự chữ, tiếp theo ta sẽ thêm 4 ký tự số
        for (int i = 0; i < 4; i++){
            int indexInteger = (int)(integer.length()*Math.random());
            idBuilder.append(integer.charAt(indexInteger));
        }

        // Cuối cùng sẽ trả về một chuỗi isBuilder bao gồm 4 số 4 chữ ngẫu nhiên
        return idBuilder.toString();
    }

    @Override
    public Object createBookingOrWaiting(WaitingListEntity waitingRequest) {
        String chargingPostID = waitingRequest.getChargingPost().getIdChargingPost();
        // Check trạng thái hiện tại của trạm sạc
        Optional<BookingEntity> latestStatusChargingPost = bookingRepository
                .findFirstByChargingPost_IdChargingPostAndStatusInOrderByCreatedAtAsc(chargingPostID, List.of("CONFIRMED", "PENDING"));
        // Câu lệnh trên sẽ lấy trạng thái mới nhất của trạm sạc với ID trụ sạc là chargingPostID
        // Và trạng thái của booking là "waiting" hoặc "charging"
        // Status là status của trụ ở trong bảng booking chứ không phải trong bảng chargingPost

        // Nếu trạng thái hiện tại của trạm sạc là "waiting" hoặc "charging" thì sẽ vào danh sách chờ bởi vì trạm sạc đang có người dùng

        if (latestStatusChargingPost.isEmpty()) { // Nếu trạm sạc hiện tại không có trạng thái "waiting" hoặc "charging" thì sẽ tạo booking luôn
            BookingEntity bookingEntity = new BookingEntity();
            bookingEntity.setBookingId(randomIdMethod());

            // fetch lại user từ DB để chắc chắn có userReputations
            UserEntity user = userRepository.findById(waitingRequest.getUser().getUserID())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // fetch car, chargingStation, post từ DB giống user
            CarEntity car = carRepository.findById(waitingRequest.getCar().getCarID())
                    .orElseThrow(() -> new RuntimeException("Car not found"));

            ChargingPostEntity post = chargingPostRepository.findById(waitingRequest.getChargingPost().getIdChargingPost())
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            bookingEntity.setUser(user);
            bookingEntity.setChargingStation(waitingRequest.getChargingStation());
            bookingEntity.setChargingPost(post);
            bookingEntity.setCar(car);
            bookingEntity.setCreatedAt(LocalDateTime.now());

            // Lấy maxWaitingTime từ bảng user_reputation
            // Lấy tất cả các record trong bảng user_reputation của user, sau đó lấy max của maxWaitMinutes
            // Nếu không có record nào trong bảng user_reputation thì sẽ ném ra ngoại lệ
            bookingEntity.setMaxWaitingTime(user.getUserReputations()
                    .stream().mapToInt(reputation -> reputation.getReputationLevel().getMaxWaitMinutes())
                    .max().orElseThrow(() -> new RuntimeException("No reputation levels found for user")));

            bookingEntity.setStatus("CONFIRMED");
            BookingEntity savedBooking = bookingRepository.save(bookingEntity);

            simpMessagingTemplate.convertAndSendToUser( waitingRequest.getUser().getUserID(),
                    "/queue/notifications/" + chargingPostID,
                    "User " + waitingRequest.getUser().getUserID() + " booked successfully");

            return savedBooking;

        }

        else{
            // Khi có các trạng thái "waiting" hoặc "charging" thì sẽ vào danh sách chờ
            // Lưu record vào bảng waitingList
            WaitingListEntity savedWaiting = waitingListService.addToWaitingList(waitingRequest);

            // Thêm userID vào danh sách chờ trong Redis
//            redisTemplate.opsForList().rightPush(redisKey(chargingPostID), waitingRequest.getUser().getUserID());

            // Trả về thông báo tới user rằng đã vào danh sách chờ
            // Nếu muốn gửi thông báo đến đích danh người dùng thì sẽ gửi đến kênh "/user/{userID}/queue/notifications"
            // Muốn lấy số thứ tự của user trong danh sách chờ thì sẽ lấy vị trí của userID trong danh sách Redis
            int positionInQueue = redisTemplate.opsForList().range(redisKey(chargingPostID), 0, -1).indexOf(waitingRequest.getUser().getUserID()) + 1;
            simpMessagingTemplate.convertAndSendToUser(waitingRequest.getUser().getUserID(), "/queue/notifications",
                    "Your position in queue: " + positionInQueue);

            return savedWaiting; // Trả về null vì không tạo booking mà chỉ vào danh sách chờ
        }
    }



    @Override
    public BookingEntity completeBooking(String bookingID) {
        BookingEntity booking = bookingRepository.findById(bookingID).orElseThrow();

        booking.setStatus("COMPLETED");
        booking.setDoneAt(LocalDateTime.now());

        // Gửi thông báo đến user rằng booking đã hoàn thành
        simpMessagingTemplate.convertAndSendToUser(booking.getUser().getUserID(),"/queue/notifications",
                "Your Booking: " + booking.getBookingId() + " completed successfully");

        return bookingRepository.save(booking);
    }

    @Override
    public BookingEntity cancelBooking(String bookingID) {
        BookingEntity booking = bookingRepository.findById(bookingID).orElseThrow();

        booking.setStatus("CANCELLED");
        booking.setDoneAt(LocalDateTime.now());

        // Gửi thông báo đến user rằng booking đã bị hủy
        simpMessagingTemplate.convertAndSendToUser(booking.getUser().getUserID(), "/queue/notifications",
                "Your Booking: " + booking.getBookingId() + " cancel booking successfully");

        return bookingRepository.save(booking);
    }



    @Transactional
    public BookingEntity processBooking(String chargingPostID) {

        boolean busy = bookingRepository
                .findFirstByChargingPost_IdChargingPostAndStatusInOrderByCreatedAtAsc(
                        chargingPostID, List.of("CONFIRMED", "PENDING", "CHARGING"))
                .isPresent();
        if (busy) {
            throw new IllegalStateException("Charging post " + chargingPostID + " is still busy");
        }

        WaitingListEntity waitingList = waitingListRepository
                .findFirstByChargingPost_IdChargingPostAndStatusOrderByCreatedAtAsc(
                        chargingPostID, "WAITING")
                .orElseThrow(() -> new RuntimeException("No WAITING record for post " + chargingPostID));


        BookingEntity booking = new BookingEntity();
        booking.setBookingId(randomIdMethod());
        booking.setWaitingList(waitingList);
        booking.setUser(waitingList.getUser());
        booking.setChargingStation(waitingList.getChargingStation());
        booking.setChargingPost(waitingList.getChargingPost());
        booking.setCar(waitingList.getCar());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setMaxWaitingTime(
                waitingList.getUser().getUserReputations().stream()
                        .mapToInt(r -> r.getReputationLevel().getMaxWaitMinutes())
                        .max().orElse(15) // fallback
        );
        booking.setStatus("CONFIRMED");
        BookingEntity saved = bookingRepository.save(booking);

        waitingList.setStatus("COMPLETED");
        waitingList.setOutedAt(LocalDateTime.now());
        waitingListRepository.save(waitingList);

        String popped = redisTemplate.opsForList().leftPop(redisKey(chargingPostID));

        simpMessagingTemplate.convertAndSendToUser(
                waitingList.getUser().getUserID(),
                "/queue/notifications",
                "Your booking " + saved.getBookingId() + " has been confirmed."
        );

        return saved;
    }


//    @Override
//    public BookingEntity processBooking(String chargingPostID) {
//        String userID = redisTemplate.opsForList().leftPop(redisKey(chargingPostID));
//        // Khi hàm này được gọi, nó sẽ lấy userID của user đầu tiên trong danh sách chờ của trạm sạc tương ứng
//        // leftPop là hàm để lấy và xóa phần tử đầu tiên trong danh sách
//        if (userID == null) {
//            throw new RuntimeException("No users in waiting list");
//        }
//
//        // Trước tiên phải tìm được record về user trong waitingList
//        WaitingListEntity waitingListEntity = waitingListRepository.findFirstByChargingPost_IdChargingPostAndStatusOrderByCreatedAtDesc(
//                chargingPostID, "WAITING").stream() // Lấy tất cả các record trong waitingList của trạm sạc với status là "WAITING"
//                // với stream() là để
//                .filter(waitingList -> waitingList.getUser().getUserID().equals(userID)) // Lọc ra record có userID trùng với userID vừa lấy từ Redis
//                // Cú pháp của filter là (biến tạm thời) -> (điều kiện lọc), biến tạm thời là từng phần tử trong stream
//                // Ở đây, biến tam thời là waitingList, điều kiện lọc là waitingList.getUser().getUserID().equals(userID)
//                .findFirst() // Lấy ra phần tử đầu tiên trong stream sau khi đã lọc
//                .orElse(null); // Nếu không có phần tử thỏa mãn thì trả về null
//
//        // Sau khi lấy được phần tử trong waitingList, ta sẽ chuyển trạng thái của nó từ "WAITING" sang "BOOKED" và tạo một record mới trong bảng booking
//        waitingListEntity.setOutedAt(LocalDateTime.now());
//        waitingListEntity.setStatus("COMPLETED");
//        waitingListRepository.save(waitingListEntity);
//
//        // Sau khi lưu DB xong cho phần waitingList, ta sẽ tạo một record mới trong DB của Booking
//        BookingEntity bookingEntity = new BookingEntity();
//        bookingEntity.setBookingId(randomIdMethod());
//        bookingEntity.setWaitingList(waitingListEntity);
//        bookingEntity.setUser(waitingListEntity.getUser());
//        bookingEntity.setChargingStation(waitingListEntity.getChargingStation());
//        bookingEntity.setChargingPost(waitingListEntity.getChargingPost());
//        bookingEntity.setCar(waitingListEntity.getCar());
//        bookingEntity.setCreatedAt(LocalDateTime.now());
//        bookingEntity.setMaxWaitingTime(waitingListEntity.getUser().getUserReputations()
//                                        .stream().mapToInt(reputation -> reputation.getReputationLevel().getMaxWaitMinutes())
//                                        .max().orElseThrow(() -> new RuntimeException("No reputation levels found for user")));
//        bookingEntity.setStatus("CONFIRMED");
//        BookingEntity savedBooking = bookingRepository.save(bookingEntity);
//
//        simpMessagingTemplate.convertAndSendToUser(waitingListEntity.getUser().getUserID(), "/queue/notifications" + chargingPostID, "User moved to booking");
//        // Gửi thông báo realtime đến tất cả các client đang lắng nghe kênh "/topic/waiting/{chargingPostId}"
//        // Để thông báo rằng có một user đã được chuyển từ danh sách chờ sang đặt chỗ
//
//        return savedBooking;
//    }




>>>>>>> 28dd984 (Code about Waiting and Booking Service)
}
