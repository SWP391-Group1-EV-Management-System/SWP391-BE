package charging_manage_be.services.car;

import charging_manage_be.model.dto.car.CarRequestDTO;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.cars.CarRepository;
import charging_manage_be.repository.payments.PaymentMethodRepository;
import charging_manage_be.repository.users.UserRepository;
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
    @Autowired
    private UserRepository userRepository;

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
    public CarEntity insertCar(CarRequestDTO carRequestDTO) {
        UserEntity user = userRepository.findById(carRequestDTO.getUser()).orElse(null);
        CarEntity newCar = new CarEntity();
        newCar.setCarID(generateUniqueId());
        newCar.setLicensePlate(carRequestDTO.getLicensePlate());
        newCar.setUser(user);
        newCar.setTypeCar(carRequestDTO.getTypeCar());
        newCar.setChassisNumber(carRequestDTO.getChassisNumber());
//        newCar.setChargingType(carRequestDTO.getChargingType());
        carRepository.save(newCar);
        return newCar;
    }

    @Override
    public CarEntity updateCar(String carId, CarRequestDTO carEntity) {
        return null;
    }

    @Override
    public boolean deleteCarByCarID(String carID) {
        return false;
    }

    @Override
    public CarEntity getCarByCarID(String carID) {
        return null;
    }

    @Override
    public List<CarEntity> findAllCar() {
        return List.of();
    }

    @Override
    public List<CarEntity> findAllCarByUserID(String userID) {
        UserEntity user  = userService.getUserByID(userID).orElse(null);
        return carRepository.findByUser(user);
    }
}
