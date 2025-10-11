package charging_manage_be.services.car;

import charging_manage_be.model.dto.car.CarRequestDTO;
import charging_manage_be.model.dto.car.CarResponseDTO;
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.cars.CarRepository;
import charging_manage_be.repository.charging_type.ChargingTypeRepository;
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
    @Autowired
    private ChargingTypeRepository chargingTypeRepository;

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
    public boolean insertCar(CarRequestDTO carRequestDTO) {
        UserEntity user = userRepository.findById(carRequestDTO.getUser()).orElse(null);
        ChargingTypeEntity chargingType = chargingTypeRepository.findById(carRequestDTO.getChargingType()).orElse(null);

        CarEntity newCar = new CarEntity();
        newCar.setCarID(generateUniqueId());
        newCar.setLicensePlate(carRequestDTO.getLicensePlate());
        newCar.setUser(user);
        newCar.setTypeCar(carRequestDTO.getTypeCar());
        newCar.setChassisNumber(carRequestDTO.getChassisNumber());
        newCar.setChargingType(chargingType);
        carRepository.save(newCar);

        return true;
    }

    @Override
    public boolean updateCar(String carId, CarRequestDTO carEntity) {
        CarEntity updatedCar = carRepository.findById(carId).orElse(null);
        if (updatedCar != null) {
            UserEntity user = userRepository.findById(carEntity.getUser()).orElse(null);
            ChargingTypeEntity chargingType = chargingTypeRepository.findById(carEntity.getChargingType()).orElse(null);
            updatedCar.setLicensePlate(carEntity.getLicensePlate());
            updatedCar.setUser(user);
            updatedCar.setTypeCar(carEntity.getTypeCar());
            updatedCar.setChassisNumber(carEntity.getChassisNumber());
            updatedCar.setChargingType(chargingType);
            carRepository.save(updatedCar);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteCarByCarID(String carID) {
        if (carRepository.findById(carID).isPresent()) {
            carRepository.deleteById(carID);
        }
        else{
            return false;
        }
        return true;
    }

    @Override
    public CarEntity getCarByCarID(String carID) {
        return carRepository.findById(carID).orElse(null);
    }


    @Override
    public List<CarEntity> findAllCarByUserID(String userID) {
        UserEntity user = userService.getUserByID(userID).orElse(null);
        return carRepository.findByUser(user);
    }
}
