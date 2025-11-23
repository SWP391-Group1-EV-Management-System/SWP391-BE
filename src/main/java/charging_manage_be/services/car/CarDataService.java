package charging_manage_be.services.car;

import charging_manage_be.model.entity.cars.CarDataEntity;

import java.util.List;
import java.util.Optional;

public interface CarDataService {
    CarDataEntity addCarData(CarDataEntity carDataEntity);
    boolean updateCarData(int carId, CarDataEntity carDataEntity);
    boolean deleteCarData(CarDataEntity carDataEntity);
    CarDataEntity getCarDataById(int carId);
    List<CarDataEntity> getAllCarData();

}
