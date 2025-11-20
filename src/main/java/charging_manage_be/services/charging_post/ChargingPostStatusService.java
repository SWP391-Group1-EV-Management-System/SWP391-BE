package charging_manage_be.services.charging_post;

import java.util.Map;

public interface ChargingPostStatusService {
    public void broadcastPostStatus(String postId);

    public Map<String, Object> getPostStatus(String postId);
    public void broadcastStationStatus(String stationId);
}
