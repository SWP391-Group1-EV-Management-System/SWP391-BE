package charging_manage_be;

import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.services.payments.PaymentServiceImpl;
import charging_manage_be.services.users.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.math.BigDecimal;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
//        System.setProperty("spring.main.web-application-type", "none");
//        ApplicationContext context = SpringApplication.run(Main.class, args);
//
//        // Inject UserService
//        UserService userService = context.getBean(UserService.class);
//        PaymentServiceImpl paymentService = context.getBean(PaymentServiceImpl.class);
//
//        UserEntity user = new UserEntity(
//                "John",
//                "Doe",
//                new Date(),
//                new Random().nextBoolean(),
//                "USER",
//                "john.doe" + new Random().nextInt(1000) + "@example.com",
//                UUID.randomUUID().toString(),
//                "09" + (10000000 + new Random().nextInt(90000000)),
//                LocalDateTime.now(),
//                "ACTIVE",
//                null, // cars
//                null  // userReputations
//        );
//        user.setUserID("ABC123");
//
//        // Sửa thành userService.addUser()
//        boolean saveUser = userService.addUser(user);
//        if (!saveUser) {
//            System.out.println("Failed to save user.");
//            return;
//        } else {
//            System.out.println("User saved successfully.");
//        }
//
//        paymentService.createPayment(user, "LanSac1", new BigDecimal("120000"));
//        System.out.print("Success");
        SpringApplication.run(Main.class, args);
    }
}