package charging_manage_be;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@SpringBootApplication
public class Main {
    public static void main(String[] args) {

      SpringApplication.run(Main.class, args); // Đây là test để chạy Spring Boot mà không cần server để test trên Postman

        // Đoạn này là test service và repository của User_Reputation bằng việc input dữ liệu mẫu và in ra kết quả
//        UserReputationService userReputationService = context.getBean(UserReputationService.class);
//
//        List<User_ReputationEntity> reps = userReputationService.getUserReputationByUserID("180305");
//
//        // Đoạn này là để chuyển đổi từ Entity sang những gì mình cần lấy ra
//        // Ví dụ ở đây chỉ cần in ra tên user, level name, max wait và createdAt
//        reps.forEach(r -> {
//            System.out.println(
//                    "User: " + r.getUser().getFirstName() + // Đoạn .getUser() là lấy về UserEntity, sau đó .getFirstName() là lấy tên
//                            // Tương tự cho những cái bên dưới
//                            " | Level: " + r.getReputationLevel().getLevelName() +
//                            " | Max Wait Time: " + r.getReputationLevel().getMaxWaitMinutes() +
//                            " | CreatedAt: " + r.getCreatedAt()
//            );
//        });




    }
}