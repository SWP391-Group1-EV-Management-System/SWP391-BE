package charging_manage_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
//        System.setProperty("spring.main.web-application-type", "none");
//        ApplicationContext context = SpringApplication.run(Main.class, args);

        // Đoạn này là test service và repository của Payment bằng việc input dữ liệu mẫu và in ra kết quả
        // Inject UserService
//        UserService userService = context.getBean(UserService.class);
//        PaymentServiceImpl paymentService = context.getBean(PaymentServiceImpl.class);
//        paymentService.invoicePayment("JAPW8217", "VNPAY");
//        UserEntity user = new UserEntity(
//                "Khanh",
//                "Việt",
//                new Date(), // dateOfBirth
//                new Random().nextBoolean(), // Gender
//                "USER", // role
//                "khanhpm12123@gmail.com", // email
//                UUID.randomUUID().toString(), // password
//                "0910238902", // phoneNumber
//                LocalDateTime.now(), // createdAt
//                "ACTIVE", // status
//                null, // cars
//                null  // userReputations
//        );
//        user.setUserID("180305");
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
//        paymentService.createPayment(user, "Sạc lần 1", new BigDecimal("10000000"));
//        System.out.print("Success");



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