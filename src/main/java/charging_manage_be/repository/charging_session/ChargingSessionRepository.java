package charging_manage_be.repository.charging_session;

import charging_manage_be.model.entity.Charging.ChargingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargingSessionRepository extends JpaRepository<ChargingSessionEntity, String> {

}
