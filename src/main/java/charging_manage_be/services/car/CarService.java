package charging_manage_be.services.car;

import charging_manage_be.model.dto.car.CarRequestDTO;
import charging_manage_be.model.dto.car.CarResponseDTO;
import charging_manage_be.model.entity.cars.CarEntity;

import java.util.List;
import java.util.Optional;

public interface CarService {

    boolean insertCar(CarRequestDTO carRequestDTO);
    boolean updateCar(String carId, CarRequestDTO carRequestDTO);
    boolean deleteCarByCarID(String carID);
    CarEntity getCarByCarID(String carID);
    List<CarEntity> findAllCarByUserID(String userID);
}
