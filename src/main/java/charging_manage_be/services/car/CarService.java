package charging_manage_be.services.car;

import charging_manage_be.model.dto.car.CarRequestDTO;
import charging_manage_be.model.dto.car.CarResponseDTO;
import charging_manage_be.model.entity.cars.CarEntity;

import java.util.List;
import java.util.Optional;

public interface CarService {

    boolean insertCarByUser(CarRequestDTO carRequestDTO);
    boolean insertCarByAdmin(CarRequestDTO carRequestDTO);
    boolean updateCarByUser(String carId, CarRequestDTO carRequestDTO);
//    boolean updateCarByAdmin(String carId, CarRequestDTO carRequestDTO);
    boolean deleteCarByCarID(String carID);
    CarEntity getCarByCarID(String carID);
    List<CarEntity> findAllCar();
    int pinRandom();
    int maxMinutes(int pinRandom);
    void storeCurrentPin(String userId, int currentPin);
    int calculateMaxSeconds(int currentPin, int targetPin);

    List<CarEntity> findAllActiveCarsByUserID(String userID);
    List<CarEntity> findByLicensePlate(String licensePlate);
    List<CarEntity> findByChassisNumber(String chassisNumber);
    CarEntity findByChassisNumberAndUserIDAndActive(String chassisNumber, String userID);
}
