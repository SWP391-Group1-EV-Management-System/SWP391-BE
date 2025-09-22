package charging_manage_be.repository.cars;

import charging_manage_be.model.entity.cars.CarEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CarRepositoryImpl implements CarRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public boolean addCar(CarEntity car) {
        try {
            entityManager.persist(car);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateCar(CarEntity car) {
        try{
            entityManager.merge(car);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteCar(String carID) {
        try{
            CarEntity car = entityManager.find(CarEntity.class, carID);
            if(car != null){
                entityManager.remove(car);
                return true;
            }
            return false;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public CarEntity getCarByID(String carID) {
        try {
            return entityManager.find(CarEntity.class, carID);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<CarEntity> getAllCars() {
        try {
            return entityManager.createQuery("from CarEntity", CarEntity.class).getResultList();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
