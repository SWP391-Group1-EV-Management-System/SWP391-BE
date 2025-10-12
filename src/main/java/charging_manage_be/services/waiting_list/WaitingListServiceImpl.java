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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import static charging_manage_be.util.RandomId.generateRandomId;

@Service
@RequiredArgsConstructor
public class WaitingListServiceImpl implements WaitingListService{
    private static final String KEY_QUEUE_POST = "queue:post:";
    private final WaitingListRepository waitingListRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final ChargingPostRepository chargingPostRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final UserService userService;
    private  final ChargingSessionService chargingSessionService;
    // RedisTemplate là một lớp trong Spring Data Redis, nó cung cấp các phương thức để tương tác với Redis
    // Ở đây, RedisTemplate<String, String> có nghĩa là cả key và value trong Redis đều là String
    private final SimpMessagingTemplate simpMessagingTemplate;
    // SimpMessagingTemplate là một lớp trong Spring Framework, nó cung cấp các phương thức để gửi tin nhắn qua WebSocket
    // Tin nhắn ở đây là các thông báo realtime khi có sự kiện mới xảy ra, ví dụ như có user mới vào danh sách chờ

    private int characterLength = 5;
    private int numberLength = 5;

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



        simpMessagingTemplate.convertAndSendToUser(waitingListEntity.getUser().getUserID(), "/queue/notifications" + savedEntity.getChargingPost().getIdChargingPost(),
                "User " + savedEntity.getUser().getFirstName() + " joined waiting list");

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
        getPositionAllDriver(waitingListId);

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

    public void getPositionAllDriver(String queueName) {
        String key = "queue:" + queueName;
        List<String> queue = redisTemplate.opsForList().range(key, 0, -1);
        for (int i = 0; i < queue.size(); i++) {
            String userId = queue.get(i); // sau này có thể đổi lại thành session id
            int position = i + 1;
//            simpMessagingTemplate.convertAndSend(
//                    "/topic/queue/" + queueName + "/" + uid,
//                    "Bạn đang ở vị trí số " + position
//            );
            simpMessagingTemplate.convertAndSendToUser(userId,
                    "/queue/notifications/" + queueName,
                    "Bạn đang ở vị trí số " + position);
        }
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
        waiting.setExpectedWaitingTime(expectedWaitingTime);
        waitingListRepository.save(waiting);
        return true;
    }
}
/*
@Service
public class WaitingListServiceImpl {
    private int characterLength = 5;
    private int numberLength = 5;

    @Autowired
    private BookingServiceImpl bookingService;
    @Autowired
    private WaitingListRepository waitingListRepository;
    @Autowired
    private  StringRedisTemplate redisTemplate;
    @Autowired
    private  WaitingListRepository  logRepository;
    @Autowired
    private  SimpMessagingTemplate messagingTemplate;


    public String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (isIdExists(newId));
        return newId;
    }


    public boolean isIdExists(String id) {
        return waitingListRepository.existsById(id);
    }
        // User join queue
        public boolean joinWaitingList(BookingEntity booking) {//bỏ vào database
//
//            redisTemplate.opsForList().rightPush(key, booking.getUser().getUserID());
            WaitingListEntity waiting = new WaitingListEntity();
            // tạo hàng đợi lưu xuống data
            waiting.setWaitingListId(generateUniqueId());
            waiting.setUser(booking.getUser());
            waiting.setChargingPost(booking.getChargingPost());
            waiting.setChargingStation(booking.getChargingStation());
            waiting.setStatus("waiting");
            // gọi hàm update thứ tự sạc
            waiting.setCar(booking.getCar());
            logRepository.save(waiting);
            // broadcast
            /*
            messagingTemplate.convertAndSend("/topic/queue/" + booking.getChargingStation().getIdChargingStation(),
                    "User " + booking.getUser().getUserID() + " joined. Position: " + (pos != null ? pos + 1 : -1));
            */
/*
            return true;
                    }

// User rời hàng (pop đầu queue)
public boolean popQueue(String queueName) {
    String key = "queue:" + queueName;

    String userId = redisTemplate.opsForList().leftPop(key); // lấy thằng ở số 1 sau đó update lại trạng thái cho nó

    if (userId != null) {
        //getReferenceById(userId); tạo bản fake để thao tác nhưng nhược điểm không check được user đó có tồn tại hay không
        // bởi vì nó không query như find
        WaitingListEntity waiting = waitingListRepository.findWaitingListByUserID(userId);
        waiting.setStatus("completed");
        waiting.setQuitAt(LocalDateTime.now());
        logRepository.save(waiting);
        // sau khi pop thì tạo booking cho nó
        bookingService.handleAfterWaitingList(waiting);
        // thông báo cho các tài xế đang trong hàng đợi tức trong kênh /topic/queue/" + queueName/uid
        getPositionAllDriver(queueName);

        // XỬ LÝ TRƯỜNG HỢP USER TẮT APP BẰNG EVENT LISTENER
        return true;
    }
    return false;
}

// Lấy vị trí user
public boolean outQueue(String queueName, String userId) {
    String key = "queue:" + queueName;
    Long removed = redisTemplate.opsForList().remove(key, 1, userId); // xóa đúng 1 thằng userId trong hàng đợi
    if (removed != null && removed > 0) {
        WaitingListEntity waiting = waitingListRepository.findWaitingListByUserID(userId);
        waiting.setStatus("canceled");
        logRepository.save(waiting);
        // thông báo cho các tài xế đang trong hàng đợi tức trong kênh /topic/queue/" + queueName/uid
        getPositionAllDriver(queueName);
        return true;
    }
    return false;
}
public boolean clearQueue(String queueName) {
    String key = "queue:" + queueName;
    redisTemplate.delete(key);
    return true;
}
public void getPositionAllDriver(String queueName) {
    String key = "queue:" + queueName;
    List<String> queue = redisTemplate.opsForList().range(key, 0, -1);
    for (int i = 0; i < queue.size(); i++) {
        String uid = queue.get(i);
        int position = i + 1;
        messagingTemplate.convertAndSend(
                "/topic/queue/" + queueName + "/" + uid,
                "Bạn đang ở vị trí số " + position
        );
    }
}


}
 */