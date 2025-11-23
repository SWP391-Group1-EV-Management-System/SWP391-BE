package charging_manage_be.services.car;

import charging_manage_be.model.entity.cars.CarDataEntity;
import charging_manage_be.repository.cars.CarDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarDataServiceImpl implements CarDataService {
    @Autowired
    private CarDataRepository carDataRepository;

    @Override
    public CarDataEntity addCarData(CarDataEntity carDataEntity) {
        return carDataRepository.save(carDataEntity);
    }

    @Override
    public boolean updateCarData(int carId, CarDataEntity carDataEntity) {
        CarDataEntity car = carDataRepository.findById(carId).orElse(null);
        if(car == null){
            return false;
        }
        car.setCarName(carDataEntity.getCarName());
        car.setChargingType(carDataEntity.getChargingType());
        carDataRepository.save(car);
        return true;
    }

    @Override
    public boolean deleteCarData(CarDataEntity carDataEntity) {
        carDataRepository.delete(carDataEntity);
        return true;
    }

    @Override
    public CarDataEntity getCarDataById(int carId) {
        return carDataRepository.findById(carId).orElse(null);
    }

    @Override
    public List<CarDataEntity> getAllCarData() {
        return carDataRepository.findAll();
    }
}
