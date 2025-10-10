package charging_manage_be.services.car;

import charging_manage_be.model.entity.cars.CarEntity;

import java.util.List;
import java.util.Optional;

public interface CarService {

    CarEntity insertCar(CarEntity carEntity);
    CarEntity updateCar(CarEntity carEntity);
    boolean deleteCarByCarID(String carID);
    Optional<CarEntity> getCarByCarID(String carID);
    List<CarEntity> findAllCar();
    List<CarEntity> findAllCarByUserID(String userID);
}
