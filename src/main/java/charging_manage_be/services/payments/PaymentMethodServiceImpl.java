package charging_manage_be.services.payments;

import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.payments.PaymentMethodEntity;
import charging_manage_be.repository.payments.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {



    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    private int characterLength = 5;
    private int numberLength = 5;

    public String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (paymentMethodRepository.existsById(newId));
        return newId;
    }

    @Override
    public boolean insertPaymentMethod(String methodName) {
        if (methodName == null || methodName.isEmpty()){
            throw new IllegalArgumentException("Payment method name cannot be null or empty");
        }
        PaymentMethodEntity paymentMethod = new PaymentMethodEntity();
        paymentMethod.setIdPaymentMethod(generateUniqueId());
        paymentMethod.setNamePaymentMethod(methodName);

        paymentMethodRepository.save(paymentMethod);
        return true;

    }

    @Override
    public boolean updatePaymentMethod(PaymentMethodEntity paymentMethod) {
        if (paymentMethod == null){
            throw new IllegalArgumentException("Payment method cannot be null");
        }
        else if (!paymentMethodRepository.existsById(paymentMethod.getIdPaymentMethod())){
            throw new IllegalArgumentException("Payment method with ID " + paymentMethod.getIdPaymentMethod() + " does not exist");
        }

        else{
            paymentMethodRepository.save(paymentMethod);
            return true;
        }
    }

    @Override
    public boolean deletePaymentMethod(String paymentMethodId) {
        if (!paymentMethodRepository.existsById(paymentMethodId)){
            throw new IllegalArgumentException("Payment method with ID " + paymentMethodId + " does not exist");
        }
        paymentMethodRepository.deleteById(paymentMethodId);
        return true;
    }

    @Override
    public Optional<PaymentMethodEntity> getPaymentMethodById(String paymentMethodId) {
        if (paymentMethodId == null || paymentMethodId.isEmpty()){
            throw new IllegalArgumentException("Payment method ID cannot be null or empty");
        }
        else if (!paymentMethodRepository.existsById(paymentMethodId)){
            throw new IllegalArgumentException("Payment method with ID " + paymentMethodId + " does not exist");
        }
        else{
            return paymentMethodRepository.findById(paymentMethodId);
        }
    }

    @Override
    public List<PaymentMethodEntity> getAllPaymentMethod() {
        return paymentMethodRepository.findAll();
    }
}
