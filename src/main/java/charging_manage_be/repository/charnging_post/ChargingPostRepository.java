package charging_manage_be.repository.charnging_post;

import charging_manage_be.model.entity.Charging.ChargingPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargingPostRepository extends JpaRepository<ChargingPostEntity, String> {

}
