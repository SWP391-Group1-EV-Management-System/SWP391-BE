package charging_manage_be.repository.charging_post;

import charging_manage_be.model.dto.booking.WaitingListResponseDTO;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Repository
public interface ChargingPostRepository extends JpaRepository<ChargingPostEntity, String> {
}
