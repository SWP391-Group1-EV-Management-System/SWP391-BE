package charging_manage_be;

import charging_manage_be.services.payments.PaymentServiceImpl;

import java.math.BigDecimal;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        PaymentServiceImpl paymentService = new PaymentServiceImpl("JpaName");
        paymentService.createPayment( "Bao123", "LanSac1", new BigDecimal("120000"));
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter payment id: ");
        String paymentId = scanner.nextLine();
        paymentService.invoicePayment(paymentId);

    }
}