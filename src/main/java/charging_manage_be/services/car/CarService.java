package charging_manage_be.services.car;

import charging_manage_be.model.entity.cars.CarEntity;

import java.util.List;
import java.util.Optional;

public interface CarService {

    CarEntity insertCar(CarEntity carEntity);
    CarEntity updateCar(CarEntity carEntity);
    boolean deleteCarByLicensePlate(String licensePlate);
    Optional<CarEntity> getCarByCarId(String carId);
    List<CarEntity> findAllCar();
}
