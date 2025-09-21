package charging_manage_be;

import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.services.payments.PaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.math.BigDecimal;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@SpringBootApplication
@ComponentScan(basePackages = "charging_manage_be")
@EntityScan("charging_manage_be.model.entity")
@EnableJpaRepositories("charging_manage_be.repository")
public class Main {
    public static void main(String[] args) {
        System.setProperty("spring.main.web-application-type", "none");
        ApplicationContext context = SpringApplication.run(Main.class, args);
        PaymentServiceImpl paymentService = context.getBean(PaymentServiceImpl.class);

        UserEntity userEntity = new UserEntity();
        userEntity.setUserID("Bao123");
        paymentService.createPayment( userEntity, "LanSac1", new BigDecimal("120000"));
        //Scanner scanner = new Scanner(System.in);
        System.out.print("Success");
        //String paymentId = scanner.nextLine();
        //paymentService.invoicePayment(paymentId);

    }
}