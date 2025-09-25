package charging_manage_be.repository.charging_type;

import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargingTypeRepository extends JpaRepository<ChargingTypeEntity, Integer> {
}
