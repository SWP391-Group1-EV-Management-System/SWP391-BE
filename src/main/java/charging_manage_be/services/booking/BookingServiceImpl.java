//CODE CỦA BẢO
package charging_manage_be.services.booking;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.services.car.CarServiceImpl;
import charging_manage_be.services.charging_post.ChargingPostServiceImpl;
import charging_manage_be.services.charging_station.ChargingStationServiceImpl;
import charging_manage_be.services.user_reputations.UserReputationServiceImpl;
import charging_manage_be.services.users.UserServiceImpl;
import charging_manage_be.services.waiting_list.WaitingListServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class BookingServiceImpl {
    private final int characterLength = 5;
    private final int numberLength = 5;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    @Lazy
    private WaitingListServiceImpl waitingListService;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private ChargingPostServiceImpl chargingPostService;
    @Autowired
    private ChargingStationServiceImpl chargingStationService;
    @Autowired
    private CarServiceImpl carService;
    @Autowired
    private UserReputationServiceImpl userReputationService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (bookingRepository.existsById(newId));
        return newId;
    }
    public Long handleBookingNavigation(String userId, String postId, String carId) {
        String key = "queue:" + postId;

        BookingEntity booking = new BookingEntity();
        redisTemplate.opsForList().rightPush(key, userId); // push vào queue redis
        Long pos = redisTemplate.opsForList().indexOf(key, userId);
        // lấy vị trí trong quêue và trã về nếu cần
        Optional<UserEntity> userOpt = userService.getUserByID(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User does not exist");
        }
        booking.setUser(userOpt.get());
        booking.setBookingId(userId);
        booking.setChargingPost(chargingPostService.getChargingPostById(postId));
        booking.setChargingStation(chargingStationService.findStationByChargingPostEntity(postId)); // cần lấy từ post
        booking.setCar(carService.getCarByCarId(carId).get());
        boolean isAvailable = bookingRepository.isChargingPostAvailable(postId);
        if(!isAvailable) { // driver phải đợi
            //gọi add hàng đợi
            waitingListService.joinWaitingList(booking);
            return pos; // báo cho driver vị trí
        }else {
            // nếu đặt chỗ trống, thì lấy ra luôn bởi vì thằng đầu tiên này sẽ là thằng booking đang được check
            redisTemplate.opsForList().leftPop(key);
            createBooking(booking);
        }
        return -1L;
    }
    public boolean createBooking(BookingEntity booking) {
        booking.setMaxWaitingTime(userReputationService.getCurrentUserReputationById(booking.getBookingId()).get().getReputationLevel().getMaxWaitMinutes());
        booking.setBookingId(generateUniqueId()); // chỉ tạo id sau khi slot trống
        booking.setCreatedAt(LocalDateTime.now()); // nếu slot trống thì booking mới được tạo và lưu
        booking.setStatus("waiting"); // mặc định là waiting
        bookingRepository.save(booking);
        return true;

    }
    public boolean handleAfterWaitingList(WaitingListEntity waiting)
    {
        BookingEntity booking = new BookingEntity();
        booking.setUser(waiting.getUser());
        booking.setCar(waiting.getCar());
        booking.setChargingPost(waiting.getChargingPost());
        booking.setChargingStation(waiting.getChargingStation());
        booking.setMaxWaitingTime(userReputationService.getCurrentUserReputationById(waiting.getUser().getUserID()).get().getReputationLevel().getMaxWaitMinutes());
        booking.setBookingId(generateUniqueId()); // chỉ tạo id sau khi slot trống
        booking.setCreatedAt(LocalDateTime.now()); // nếu slot trống thì booking mới được tạo và lưu
        booking.setStatus("booked"); // mặc định là waiting
        booking.setWaitingList(waiting);
        bookingRepository.save(booking);
        return true;
    }
    public boolean updateBookingStatus(BookingEntity booking, String status)
    {
        if(booking == null || !bookingRepository.existsById(booking.getBookingId()))
        {
            return false;
        }
        booking.setStatus(status);
        booking.setDoneAt(LocalDateTime.now());
        bookingRepository.save(booking);
        return true;
    }
    // booked
    // canceled
    // arrived
    // time expired
    public boolean driverStartCharging(String bookingId){
        BookingEntity driverBooking = bookingRepository.findById(bookingId).get();
        driverBooking.setDoneAt(LocalDateTime.now());
        driverBooking.setStatus("arrived");
        bookingRepository.save(driverBooking);
        return true;
    }
    public boolean driverCancelBooking(String bookingId) {
        BookingEntity driverBooking = bookingRepository.findById(bookingId).get();
        driverBooking.setDoneAt(LocalDateTime.now());
        driverBooking.setStatus("canceled");
        bookingRepository.save(driverBooking);
        return true;
    }

}
