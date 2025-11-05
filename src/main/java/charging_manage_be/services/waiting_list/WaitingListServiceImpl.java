package charging_manage_be.services.waiting_list;

import charging_manage_be.controller.charging.ChargingSession;
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.repository.cars.CarRepository;
import charging_manage_be.repository.charging_post.ChargingPostRepository;
import charging_manage_be.repository.charging_station.ChargingStationRepository;
import charging_manage_be.repository.users.UserRepository;
import charging_manage_be.repository.waiting_list.WaitingListRepository;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.users.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class WaitingListServiceImpl implements WaitingListService{
    private static final String KEY_QUEUE_POST = "queue:post:";
    private final WaitingListRepository waitingListRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final ChargingPostRepository chargingPostRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final UserService userService;
    private final ChargingSessionService chargingSessionService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    // ✅ THÊM BookingService với @Lazy để tránh circular dependency
    private final charging_manage_be.services.booking.BookingService bookingService;

    private int characterLength = 5;
    private int numberLength = 5;

    // Constructor với @Lazy cho BookingService
    @Autowired
    public WaitingListServiceImpl(
        WaitingListRepository waitingListRepository,
        RedisTemplate<String, String> redisTemplate,
        UserRepository userRepository,
        CarRepository carRepository,
        ChargingPostRepository chargingPostRepository,
        ChargingStationRepository chargingStationRepository,
        UserService userService,
        ChargingSessionService chargingSessionService,
        SimpMessagingTemplate simpMessagingTemplate,
        @Lazy charging_manage_be.services.booking.BookingService bookingService
    ) {
        this.waitingListRepository = waitingListRepository;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.chargingPostRepository = chargingPostRepository;
        this.chargingStationRepository = chargingStationRepository;
        this.userService = userService;
        this.chargingSessionService = chargingSessionService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.bookingService = bookingService;
    }

    public String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (waitingListRepository.existsById(newId));
        return newId;
    }


    private String redisKey(String chargingPostId) { // redisKey để tạo key cho danh sách chờ trong Redis
        return KEY_QUEUE_POST + chargingPostId;
    }


    @Override
    public WaitingListEntity addToWaitingList(String userId, String chargingPostId, String carId) {

            WaitingListEntity waitingListEntity = new WaitingListEntity();
            // Lưu vào DB
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // fetch car, chargingStation, post từ DB giống user
            CarEntity car = carRepository.findById(carId)
                    .orElseThrow(() -> new RuntimeException("Car not found"));

            ChargingPostEntity post = chargingPostRepository.findById(chargingPostId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            ChargingStationEntity station = chargingStationRepository.findStationByChargingPostEntity(chargingPostId)
                    .orElseThrow(() -> new RuntimeException("Station not found"));
            // xử lý trường hợp vô sau ( trụ đó có người cắm sạc và đã có expected end time trên session)
            // còn bên API bên sessionController sẽ xử lý case khi driver đợi 1 driver chưa tới trạm ( tức driver booking chưa cắm sạc chưa lấy đuọc time)
            LocalDateTime timeEnd = chargingSessionService.getExpectedEndTime(chargingPostId);
            waitingListEntity.setExpectedWaitingTime(timeEnd);
            waitingListEntity.setUser(user);
            waitingListEntity.setCar(car);
            waitingListEntity.setChargingPost(post);
            waitingListEntity.setChargingStation(station);
            waitingListEntity.setWaitingListId(generateUniqueId());
            waitingListEntity.setStatus("WAITING");
            waitingListEntity.setCreatedAt(LocalDateTime.now());

            // Push vào Redis để quản lý hàng đợi
            //redisTemplate.opsForList().rightPush(redisKey(savedEntity.getChargingPost().getIdChargingPost()), savedEntity.getUser().getUserID());
            // opsForList là hàm để thao tác với danh sách trong Redis
            // rightPush là hàm để thêm phần tử vào cuối danh sách
            // redisKey là hàm để lấy key của danh sách chờ dựa trên ID trạm sạc
            // savedEntity.getUser().getUserID() là ID của user vừa được thêm vào
            // Tức là sau thao tác này, ta sẽ có được thông tin userID được thêm vào cuối danh sách chờ của trạm sạc tương ứng trong Redis


            //Sau khi lưu vào DB và Redis thành công, ta sẽ gửi một thông báo realtime đến tất cả các client đang lắng nghe kênh "/topic/waiting/{chargingPostId}"
            // để thông báo rằng có một user mới đã được thêm vào danh sách chờ của trạm sạc tương ứng
            //simpMessagingTemplate.convertAndSend("/topic/waiting/" + savedEntity.getChargingPost().getIdChargingPost(), "New user added to waiting list");
            // convertAndSend là hàm để gửi tin nhắn đến một path cụ thể là "/topic/waiting/{chargingPostId}"
            WaitingListEntity savedEntity = waitingListRepository.save(waitingListEntity);

            // Push vào Redis để quản lý hàng đợi
            String redisKey = redisKey(savedEntity.getChargingPost().getIdChargingPost());
            String userIdToPush = savedEntity.getUser().getUserID();

            System.out.println("🔑 [addToWaitingList] Redis Key: " + redisKey);
            System.out.println("👤 [addToWaitingList] User ID: " + userIdToPush);
            System.out.println("📍 [addToWaitingList] Post ID: " + savedEntity.getChargingPost().getIdChargingPost());

            //redisTemplate.opsForList().rightPush(redisKey, userIdToPush);
            System.out.println("✅ [addToWaitingList] Pushed to Redis successfully");

            // ✅ Gửi vị trí cho TẤT CẢ user trong hàng đợi (bao gồm user vừa join)
            System.out.println("🚀 [addToWaitingList] Calling getPositionAllDriver...");
            getPositionAllDriver(savedEntity.getChargingPost().getIdChargingPost());

            return savedEntity;

    }

    @Override
    public void cancelWaiting(String waitingListId) {
        WaitingListEntity entity = waitingListRepository.findById(waitingListId).get();
        if (entity == null){
            throw  new RuntimeException("waitingListEntity is null");
        }
        entity.setStatus("CANCELLED");
        entity.setOutedAt(LocalDateTime.now());
        // Cập nhật trạng thái trong DB
        waitingListRepository.save(entity);


        // Xoá khỏi Redis
        redisTemplate.opsForList().remove(redisKey(entity.getChargingPost().getIdChargingPost()), 1, entity.getUser().getUserID());
        // remove là hàm để xoá phần tử khỏi danh sách trong Redis
        // 1 là số lượng phần tử cần xoá (1 là xoá 1 phần tử đầu tiên tìm thấy)
        // Tức là sau thao tác này, ta sẽ xoá được thông tin userID tương ứng trạm sạc khỏi danh sách chờ trong Redis

//        // Sau đó gửi một thông báo realtime đến tất cả các client đang lắng nghe kênh "/topic/waiting/{chargingPostId}"
//        // Để thông báo rằng có một user đã cancel trong hàng chờ của trạm sạc tương ứng
//        simpMessagingTemplate.convertAndSendToUser(entity.getUser().getUserID(),
//                "/queue/notification/" + entity.getChargingPost().getIdChargingPost(), "User" +entity.getUser().getFirstName()+ "cancelled");
        // chỉ cần thông báo lại vị trí cho các user khác thôi, chứ thông báo thằng A đã rơi hàng cho mấy thằng trong list để làm gì
        getPositionAllDriver(entity.getChargingPost().getIdChargingPost());

    }

    @Override
    public List<WaitingListEntity> getWaitingListForPost(String chargingPostID) {
//        return redisTemplate.opsForList().range(redisKey(chargingPostID), 0, -1);
        // range là hàm để lấy tất cả các phần tử trong danh sách từ vị trí 0 đến -1 (tức là lấy tất cả)
        // Tức là sau thao tác này, ta sẽ có được danh sách tất cả userID đang chờ theo trạm sạc tương ứng trong Redis

        // Khi lấy thông tin waitingList theo trạm sạc thì phải lấy trong DB chứ không lấy trong Redis
        ChargingPostEntity post = chargingPostRepository.findById(chargingPostID)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return waitingListRepository.findByChargingPost(post);


    }

    @Override
    public List<WaitingListEntity> getWaitingListForStation(String chargingStationID) {
        ChargingStationEntity station = chargingStationRepository.findById(chargingStationID)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return waitingListRepository.findByChargingStation(station);
    }

    @Override
    public List<WaitingListEntity> getWaitingListForUser(String userID) {
        UserEntity user = userRepository.findById(userID)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return waitingListRepository.findByUser(user);
    }

    @Override
    public WaitingListEntity getWaitingListForWaitingListId(String waitingListId) {
        return waitingListRepository.findById(waitingListId).orElse(null);
    }

    @Override
    public List<WaitingListEntity> getWaitingListForDate(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return waitingListRepository.findByCreatedAtBetween(startOfDay, endOfDay);
    }

    public void getPositionAllDriver(String postId) {
        String key = redisKey(postId); // dùng redisKey() để tạo đúng format "queue:post:{postId}"
        System.out.println("🔍 [getPositionAllDriver] Redis key: " + key);

        List<String> queue = redisTemplate.opsForList().range(key, 0, -1);
        System.out.println("📋 [getPositionAllDriver] Queue size: " + (queue != null ? queue.size() : 0));
        System.out.println("📋 [getPositionAllDriver] Queue content: " + queue);

        if (queue == null || queue.isEmpty()) {
            System.out.println("⚠️ [getPositionAllDriver] Queue is empty! No message sent.");
            return; // Không có ai trong hàng đợi
        }

        for (int i = 0; i < queue.size(); i++) {
            String userId = queue.get(i);
            int position = i + 1;
            String message = "Bạn đang ở vị trí số " + position;
            String destination = "/queue/notifications/" + postId;

            System.out.println("📤 [WebSocket] Sending to user: " + userId);
            System.out.println("📤 [WebSocket] Destination: " + destination);
            System.out.println("📤 [WebSocket] Message: " + message);

            simpMessagingTemplate.convertAndSendToUser(userId, destination, message);

            System.out.println("✅ [WebSocket] Sent successfully to " + userId);
        }
    }

    @Override
    public String getWaitingListNewByUserId(String userID) {
        return waitingListRepository.findFirstByUser_UserIDAndStatusOrderByCreatedAtDesc(userID, "WAITING").getWaitingListId();
    }

    public void updateMaxWaitingTime(String postId, String userId, LocalDateTime endTime) {
        String key = redisKey(postId); // dùng redisKey() để tạo đúng format "queue:post:{postId}"
        System.out.println("🔍 [getPositionAllDriver] Redis key: " + key);

        String message = "EndTime: " + endTime;
        String destination = "/queue/notifications/" + postId;
        System.out.println("📤 [WebSocket] Sending to user: " + userId);
        System.out.println("📤 [WebSocket] Destination: " + destination);
        System.out.println("📤 [WebSocket] Message: " + message);

        simpMessagingTemplate.convertAndSendToUser(userId, destination, message);

        System.out.println("✅ [WebSocket] Sent successfully to " + userId);
    }


    // truyền ID trụ vào sau đó check thử có ai ở vị trí đầu không rồi update expected waiting time cho nó
    @Override
    @Transactional
    public boolean addExpectedWaitingTime(String postId, LocalDateTime expectedWaitingTime) {
        String userID = redisTemplate.opsForList().index(redisKey(postId), 0);
        if (userID == null) {
            return false;
        }
        UserEntity user = userService.getUserByID(userID).orElse(null);
        // thiếu phải lấy được ID booking của thằng user đó để update expectted waiting time
        WaitingListEntity waiting  = waitingListRepository.findByUserAndStatus(user, "WAITING").orElse(null);
        //WaitingListEntity entity = waitingListRepository.findById(waitingListId).orElse(null);
        if (waiting == null) {
            return false;
        }
        // thông báo qua websocket lấy được expect waiting time
        updateMaxWaitingTime(postId, userID, expectedWaitingTime);
        waiting.setExpectedWaitingTime(expectedWaitingTime);
        waitingListRepository.save(waiting);
        return true;
    }

    @Override
    public boolean isUserWaiting(String userId) {
        boolean waiting = false;
        UserEntity user = userService.getUserByID(userId).orElse(null);
        WaitingListEntity waitingCheck = waitingListRepository.findByUserAndStatus(user, "WAITING").orElse(null);
        if(waitingCheck != null) {
            waiting = true;
        }
        return waiting;
    }

    // ✅ SCHEDULED TASK: Tự động chuyển người đầu tiên trong waiting list vào booking khi đến expectedWaitingTime
    @Scheduled(fixedRate = 10000) // Chạy mỗi 10 giây
    @Transactional
    public void processWaitingListAutoBooking() {
        try {
            // Lấy tất cả waiting list đã đến giờ và chưa được xử lý
            List<WaitingListEntity> readyToBookList = waitingListRepository
                .findByStatusAndExpectedWaitingTimeLessThanEqual("WAITING", LocalDateTime.now());

            if (readyToBookList.isEmpty()) {
                return; // Không có ai cần xử lý
            }

            System.out.println("🔔 [AUTO-PROCESS] Found " + readyToBookList.size() + " waiting entries ready to process at " + LocalDateTime.now());

            for (WaitingListEntity waiting : readyToBookList) {
                try {
                    String postId = waiting.getChargingPost().getIdChargingPost();
                    String userId = waiting.getUser().getUserID();
                    LocalDateTime expectedTime = waiting.getExpectedWaitingTime();

                    System.out.println("🔍 [AUTO-PROCESS] Checking waiting entry:");
                    System.out.println("   - User ID: " + userId);
                    System.out.println("   - Post ID: " + postId);
                    System.out.println("   - Expected Time: " + expectedTime);
                    System.out.println("   - Current Time: " + LocalDateTime.now());

                    // Kiểm tra xem user có phải người đầu tiên trong Redis queue không
                    String firstInQueue = redisTemplate.opsForList().index(redisKey(postId), 0);

                    System.out.println("   - First in Redis Queue: " + firstInQueue);

                    if (firstInQueue != null && firstInQueue.equals(userId)) {
                        System.out.println("✅ [AUTO-PROCESS] Processing booking for user: " + userId + " at post: " + postId);

                        // ✅ Gọi processBooking để tự động chuyển user vào booking
                        bookingService.processBooking(postId);

                        System.out.println("✅ [AUTO-PROCESS] Successfully processed booking for user: " + userId);
                        System.out.println("🎉 [AUTO-PROCESS] User " + userId + " has been moved from waiting list to booking!");
                    } else {
                        System.out.println("⚠️ [AUTO-PROCESS] User " + userId + " is not first in queue (first: " + firstInQueue + ")");
                        System.out.println("   This might happen if the user was already processed or removed from queue");
                    }
                } catch (Exception e) {
                    System.err.println("❌ [AUTO-PROCESS] Error processing waiting entry: " + e.getMessage());
                    e.printStackTrace();
                    // Continue với các waiting entries khác
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [AUTO-PROCESS] Fatal error in scheduled task: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
