package charging_manage_be.repository.cars;


import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.users.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<CarEntity, String> {
    List<CarEntity> findByUser(UserEntity user);
    List<CarEntity> findByLicensePlateAndIsActiveTrue(String licensePlate);
    List<CarEntity> findByChassisNumberAndIsActiveTrue(String chassisNumber);
    List<CarEntity> findByIsActiveTrueAndUser(UserEntity user);

    CarEntity findByChassisNumberAndUserAndIsActiveTrue(String chassisNumber, UserEntity user);
    CarEntity findByLicensePlateAndUserAndIsActiveTrue(String licensePlate, UserEntity user);
}
