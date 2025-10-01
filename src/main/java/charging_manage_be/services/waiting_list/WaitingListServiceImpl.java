// CODE CỦA BẢO
package charging_manage_be.services.waiting_list;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.repository.waiting_list.WaitingListRepository;
import charging_manage_be.services.booking.BookingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static charging_manage_be.util.RandomId.generateRandomId;

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
