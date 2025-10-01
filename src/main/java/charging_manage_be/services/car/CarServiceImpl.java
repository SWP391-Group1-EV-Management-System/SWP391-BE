package charging_manage_be.services.car;

import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.repository.cars.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarServiceImpl implements CarService {

    @Autowired
    private CarRepository carRepository;


    @Override
    public CarEntity insertCar(CarEntity carEntity) {
        if (carEntity == null) {
            throw new NullPointerException("carEntity is null");
        }

        else if (carRepository.existsById(carEntity.getCarID())){
            throw new IllegalStateException("carEntity already exists");
        }
        return carRepository.save(carEntity);
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
}
