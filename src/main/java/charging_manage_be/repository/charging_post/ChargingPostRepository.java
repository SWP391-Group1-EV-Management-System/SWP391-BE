package charging_manage_be.repository.charging_post;

import charging_manage_be.model.entity.charging.ChargingPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargingPostRepository extends JpaRepository<ChargingPostEntity, String> {

}
