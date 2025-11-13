package charging_manage_be.services.charging_post;

import charging_manage_be.model.dto.agent.StationAndPost;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ChargingPostService {
    public ChargingPostEntity getChargingPostById(String id);
    boolean addPost(String stationId, boolean isActive, List<Integer> listType, BigDecimal maxPower, BigDecimal chargingFeePerKWh);

    boolean updatePost(ChargingPostEntity post);
    List<ChargingPostEntity> getAllPosts();
    StationAndPost mapToDTO(ChargingStationEntity station, Double userLat, Double userLon);
    boolean isPostGotBooking(String postId);
    Map<String, Boolean> getPostAvailabilityMap(List<ChargingPostEntity> posts);

    long countActivePosts();
}
