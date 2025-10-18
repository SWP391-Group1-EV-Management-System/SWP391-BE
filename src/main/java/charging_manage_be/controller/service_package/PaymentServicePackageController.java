package charging_manage_be.controller.service_package;

import charging_manage_be.services.payments.PaymentMethodService;
import charging_manage_be.services.service_package.PaymentServicePackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment-service-packages")
public class PaymentServicePackageController {

    @Autowired
    private PaymentServicePackageService paymentServicePackageService;
    @Autowired
    private PaymentMethodService paymentMethodService;

}
