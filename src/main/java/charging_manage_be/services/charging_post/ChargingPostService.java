package charging_manage_be.services.charging_post;

import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;

import java.math.BigDecimal;
import java.util.List;

public interface ChargingPostService {
    public ChargingPostEntity getChargingPostById(String id);
    boolean addPost(String stationId, boolean isActive, List<Integer> listType, BigDecimal maxPower, BigDecimal chargingFeePerKWh);

    boolean updatePost(ChargingPostEntity post);
    List<ChargingPostEntity> getAllPosts();

}
