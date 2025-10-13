package charging_manage_be;

import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.services.booking.BookingServiceImpl;
import charging_manage_be.services.waiting_list.WaitingListServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class Main {
    public static void main(String[] args) {
//        System.setProperty("spring.main.web-application-type", "none");
//        ApplicationContext context = SpringApplication.run(Main.class, args);

        // Đoạn này là test service và repository của Payment bằng việc input dữ liệu mẫu và in ra kết quả
        // Inject UserService
//        UserService userService = context.getBean(UserService.class);
//        PaymentServiceImpl paymentService = context.getBean(PaymentServiceImpl.class);

        SpringApplication.run(Main.class, args); // Đây là test để chạy Spring Boot mà không cần server để test trên Postman
//        BookingServiceImpl bookingService = context.getBean(BookingServiceImpl.class);
//        WaitingListServiceImpl waitingListService = context.getBean(WaitingListServiceImpl.class);
        //bookingService.handleBookingNavigation("DRV001", "POST001", "CAR001");
        //bookingService.handleBookingNavigation("DRV002", "POST001", "CAR002");
        //bookingService.handleBookingNavigation("DRV003", "POST001", "CAR003");
//        bookingService.completeBooking("URGUI80727");
    }
}