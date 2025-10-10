package charging_manage_be.services.car;

import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.cars.CarRepository;
import charging_manage_be.repository.payments.PaymentMethodRepository;
import charging_manage_be.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class CarServiceImpl implements CarService {

    @Autowired
    private CarRepository carRepository;
    @Autowired
    private UserService userService;

    private int characterLength = 5;
    private int numberLength = 5;

    public String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (carRepository.existsById(newId));
        return newId;
    }


    @Override
    public CarEntity insertCar(CarEntity carEntity) {
        if (carEntity == null) {
            throw new NullPointerException("carEntity is null");
        }

        else if (carEntity.getCarID() == null) {
            carEntity.setCarID(generateUniqueId());
        }

        else if (carRepository.existsById(carEntity.getCarID())){
            throw new IllegalStateException("carEntity already exists");
        }
        return carRepository.save(carEntity); // Lúc này chỉ cần truyền những tham số cần thiết chứ không cần truyền id
    }

    @Override
    public CarEntity updateCar(CarEntity carEntity) {
        if (carEntity == null) {
            throw new NullPointerException("carEntity is null");
        }
        else if (!carRepository.existsById(carEntity.getCarID())){
            throw new IllegalStateException("carEntity is not exists");
        }
        return carRepository.save(carEntity);
    }

    @Override
    public boolean deleteCarByCarID(String carID) {
        if (carID == null) {
            throw new NullPointerException("carID is null");
        }
        else if (!carRepository.existsById(carID)){
            throw new IllegalStateException("carEntity is not exists");
        }
        else {
            carRepository.deleteById(carID);
            return true;
        }
    }

    @Override
    public Optional<CarEntity> getCarByCarID(String carID) {
        if (carID == null) {
            throw new NullPointerException("carID is null");
        }
        return carRepository.findById(carID);
    }

    @Override
    public List<CarEntity> findAllCar() {
        return carRepository.findAll();
    }

    @Override
    public List<CarEntity> findAllCarByUserID(String userID) {
        UserEntity user  = userService.getUserByID(userID).orElse(null);
        return carRepository.findByUser(user);
    }
}
