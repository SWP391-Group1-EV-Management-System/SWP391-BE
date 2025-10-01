package charging_manage_be.services.waiting_list;

<<<<<<< HEAD
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.repository.waiting_list.WaitingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class WaitingListServiceImpl {
    private int characterLength = 5;
    private int numberLength = 5;
    @Autowired
    private BookingRepository bookingRepository;
    private WaitingListRepository waitingListRepository;
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
    public boolean handleWaitingListFromBooking(BookingEntity booking) {




        WaitingListEntity waiting = new WaitingListEntity();// timestemp tự tạo lấy để xét vị trí của tài xế
        waiting.setWaitingListId(generateUniqueId());
        waiting.setUser(booking.getUser());
        waiting.setChargingPost(booking.getChargingPost());
        waiting.setChargingStation(booking.getChargingStation());
        // gọi hàm update thứ tự sạc
        waiting.setCar(booking.getCar());
        waitingListRepository.save(waiting);

    }// sủ dụng queue
    public int getWaitingPosition(String waitingPost) { // trã về vị trí hàng đợi của driver ( cập nhật liên tục)
        // update lại id của hàng đợi là long

        return 0;
    }

=======

import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.repository.waiting_list.WaitingListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WaitingListServiceImpl implements WaitingListService{

    private final WaitingListRepository waitingListRepository;
    private final RedisTemplate<String, String> redisTemplate;
    // RedisTemplate là một lớp trong Spring Data Redis, nó cung cấp các phương thức để tương tác với Redis
    // Ở đây, RedisTemplate<String, String> có nghĩa là cả key và value trong Redis đều là String
    private final SimpMessagingTemplate simpMessagingTemplate;
    // SimpMessagingTemplate là một lớp trong Spring Framework, nó cung cấp các phương thức để gửi tin nhắn qua WebSocket
    // Tin nhắn ở đây là các thông báo realtime khi có sự kiện mới xảy ra, ví dụ như có user mới vào danh sách chờ

    private String alphabet;
    private String integer;

    private String randomIdMethod(){
        // Tạo ID ngẫu nhiên cho booking với 4 ký tư tự chữ và 4 ký tự số
        alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        integer = "0123456789";
        StringBuilder idBuilder = new StringBuilder(); // StringBuilder là một lớp trong Java để xây dựng chuỗi một cách hiệu quả hơn String thông thường
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

    private String redisKey(String chargingPostId) { // redisKey để tạo key cho danh sách chờ trong Redis
        return "queue:post:" + chargingPostId;
    }


    @Override
    public WaitingListEntity addToWaitingList(WaitingListEntity waitingListEntity) {
        // Lưu vào DB
        waitingListEntity.setWaitingListId(randomIdMethod());
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

        redisTemplate.opsForList().rightPush(
                redisKey(savedEntity.getChargingPost().getIdChargingPost()),
                savedEntity.getUser().getUserID()
        );

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

        // Sau đó gửi một thông báo realtime đến tất cả các client đang lắng nghe kênh "/topic/waiting/{chargingPostId}"
        // Để thông báo rằng có một user đã cancel trong hàng chờ của trạm sạc tương ứng
        simpMessagingTemplate.convertAndSendToUser(entity.getUser().getUserID(),
                "/queue/notification/" + entity.getChargingPost().getIdChargingPost(), "User" +entity.getUser().getFirstName()+ "cancelled");

    }

    @Override
    public List<String> getWaitingListForPost(String chargingPostID) {
        return redisTemplate.opsForList().range(redisKey(chargingPostID), 0, -1);
        // range là hàm để lấy tất cả các phần tử trong danh sách từ vị trí 0 đến -1 (tức là lấy tất cả)
        // Tức là sau thao tác này, ta sẽ có được danh sách tất cả userID đang chờ theo trạm sạc tương ứng trong Redis
    }
>>>>>>> 28dd984 (Code about Waiting and Booking Service)
}
