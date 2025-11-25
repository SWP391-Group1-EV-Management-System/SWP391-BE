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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private StringRedisTemplate redisTemplate;

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
    public boolean insertCarByUser(CarRequestDTO carRequestDTO) {
        UserEntity user = userRepository.findById(carRequestDTO.getUser()).orElse(null);
        ChargingTypeEntity chargingType = chargingTypeRepository.findById(carRequestDTO.getChargingType()).orElse(null);
        List<CarEntity> carsByPlate =
                carRepository.findByLicensePlateAndIsActiveTrue(carRequestDTO.getLicensePlate());

        List<CarEntity> carsByChassis =
                carRepository.findByChassisNumberAndIsActiveTrue(carRequestDTO.getChassisNumber());

        // Nếu có ít nhất 1 xe active trùng → không cho add
        if (!carsByPlate.isEmpty() || !carsByChassis.isEmpty()) {
            return false;
        }

        CarEntity newCar = new CarEntity();
        newCar.setCarID(generateUniqueId());
        newCar.setLicensePlate(carRequestDTO.getLicensePlate());
        newCar.setUser(user);
        newCar.setTypeCar(carRequestDTO.getTypeCar());
        newCar.setChassisNumber(carRequestDTO.getChassisNumber());
        newCar.setChargingType(chargingType);
        newCar.setIsActive(true);
        carRepository.save(newCar);

        return true;
    }

    @Override
    public boolean insertCarByAdmin(CarRequestDTO carRequestDTO) {
        UserEntity user = userRepository.findById(carRequestDTO.getUser()).orElse(null);
        ChargingTypeEntity chargingType = chargingTypeRepository.findById(carRequestDTO.getChargingType()).orElse(null);

        CarEntity newCar = new CarEntity();
        newCar.setCarID(generateUniqueId());
        newCar.setLicensePlate(carRequestDTO.getLicensePlate());
        newCar.setUser(user);
        newCar.setTypeCar(carRequestDTO.getTypeCar());
        newCar.setChassisNumber(carRequestDTO.getChassisNumber());
        newCar.setChargingType(chargingType);
        newCar.setIsActive(true);
        carRepository.save(newCar);

        return true;
    }

    @Override
    public boolean updateCarByUser(String carId, CarRequestDTO carEntity) {

        CarEntity updatedCar = carRepository.findById(carId).orElse(null);
        if (updatedCar == null) {
            return false;
        }

        // Check trùng biển số
        List<CarEntity> carByPlate = carRepository.findByLicensePlateAndIsActiveTrue(carEntity.getLicensePlate());

        if(carByPlate.size() > 1 ||
                (carByPlate.size() == 1 && !carByPlate.get(0).getCarID().equals(carId))) {
            return false;
        }

        // Check trùng số khung
        List<CarEntity> carByChassis = carRepository.findByChassisNumberAndIsActiveTrue(carEntity.getChassisNumber());

        if(carByChassis.size() > 1 ||
                (carByChassis.size() == 1 && !carByChassis.get(0).getCarID().equals(carId))) {
            return false;
        }

        UserEntity user = userRepository.findById(carEntity.getUser()).orElse(null);
        ChargingTypeEntity chargingType =
                chargingTypeRepository.findById(carEntity.getChargingType()).orElse(null);

        updatedCar.setLicensePlate(carEntity.getLicensePlate());
        updatedCar.setUser(user);
        updatedCar.setTypeCar(carEntity.getTypeCar());
        updatedCar.setChassisNumber(carEntity.getChassisNumber());
        updatedCar.setChargingType(chargingType);

        carRepository.save(updatedCar);
        return true;
    }



//    @Override
//    public boolean updateCarByAdmin(String carId, CarRequestDTO carEntity) {
//
//        CarEntity updatedCar = carRepository.findById(carId).orElse(null);
//        if (updatedCar == null) {
//            return false;
//        }
//
//        CarEntity carByPlate = carRepository.findByLicensePlateAndIsActiveTrue(carEntity.getLicensePlate());
//        if (carByPlate != null && !carByPlate.getCarID().equals(carId)) {
//            return false;
//        }
//
//        CarEntity carByChassis = carRepository.findByChassisNumberAndIsActiveTrue(carEntity.getChassisNumber());
//        if (carByChassis != null && !carByChassis.getCarID().equals(carId)) {
//            return false;
//        }
//
//        UserEntity user = userRepository.findById(carEntity.getUser()).orElse(null);
//        ChargingTypeEntity chargingType =
//                chargingTypeRepository.findById(carEntity.getChargingType()).orElse(null);
//
//        updatedCar.setLicensePlate(carEntity.getLicensePlate());
//        updatedCar.setUser(user);
//        updatedCar.setTypeCar(carEntity.getTypeCar());
//        updatedCar.setChassisNumber(carEntity.getChassisNumber());
//        updatedCar.setChargingType(chargingType);
//        updatedCar.setIsActive(carEntity.isActive());
//
//        carRepository.save(updatedCar);
//        return true;
//    }


    @Override
    public boolean deleteCarByCarID(String carID) {
        CarEntity carEntity = carRepository.findById(carID).orElse(null);
        assert carEntity != null;
        carEntity.setIsActive(false);
        carRepository.save(carEntity);
        return true;
    }

    @Override
    public CarEntity getCarByCarID(String carID) {
        return carRepository.findById(carID).orElse(null);
    }


    @Override
    public List<CarEntity> findAllCar() {
        return carRepository.findAll();
    }

    @Override
    public int pinRandom() {
        return java.util.concurrent.ThreadLocalRandom.current().nextInt(1, 41);
    }
    @Override
    public int maxMinutes(int pinRandom) {
        return (int) Math.ceil(((100 - pinRandom) * 13.25) / 60.0);
    }

    @Override
    public void storeCurrentPin(String userId, int currentPin) {
        String key = "charging:preference:" + userId;
        redisTemplate.opsForHash().put(key, "currentPin", String.valueOf(currentPin));
        // TTL 30 phút
        redisTemplate.expire(key, 30, TimeUnit.MINUTES);
    }

    @Override
    public int calculateMaxSeconds(int currentPin, int targetPin) {
        // Tính số % pin cần sạc
        int pinDifference = targetPin - currentPin;
        // Mỗi 1% pin cần 13.25 giây
        return (int) Math.ceil(pinDifference * 13.25);
    }

    @Override
    public List<CarEntity> findAllActiveCarsByUserID(String userID) {
        UserEntity user = userService.getUserByID(userID).orElse(null);
        return carRepository.findByIsActiveTrueAndUser(user);
    }

    @Override
    public List<CarEntity> findByLicensePlate(String licensePlate) {
        return carRepository.findByLicensePlateAndIsActiveTrue(licensePlate);
    }

    @Override
    public List<CarEntity> findByChassisNumber(String chassisNumber) {
        return carRepository.findByChassisNumberAndIsActiveTrue(chassisNumber);
    }

    @Override
    public CarEntity findByChassisNumberAndUserIDAndActive(String chassisNumber, String userID) {
        UserEntity user = userService.getUserByID(userID).orElse(null);
        return carRepository.findByChassisNumberAndUserAndIsActiveTrue(chassisNumber, user);
    }


    // cố định 1 xe dung lượng từ 1 đến 100 là 92kW
    // trạm sạc cố định là 250KW
    // vậy với trạm 250kW thì từ 0 đến 100 sẽ mất 22p -> 13 giây sẽ sạc đuọc 1% pin
    //13.25 là đang gán cứng cho xe 92kw, 1% xe thì phải sạc 13.25s
}
