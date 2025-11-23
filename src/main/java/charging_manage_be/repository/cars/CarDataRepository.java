package charging_manage_be.repository.cars;

import charging_manage_be.model.entity.cars.CarDataEntity;
import charging_manage_be.model.entity.cars.CarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarDataRepository extends JpaRepository<CarDataEntity, Integer> {
}
