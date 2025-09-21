package charging_manage_be.repository.cars;


import charging_manage_be.model.entity.cars.CarEntity;

import java.util.List;

public interface CarRepository {
    public boolean addCar(CarEntity car);
    public boolean updateCar(CarEntity car);
    public boolean deleteCar(String carID);
    public CarEntity getCarByID(String carID);
    public List<CarEntity> getAllCars();
}
