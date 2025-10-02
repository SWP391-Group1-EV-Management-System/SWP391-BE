package charging_manage_be.services.charging_post;


import charging_manage_be.model.entity.charging.ChargingPostEntity;

public interface ChargingPostSevice {
    ChargingPostEntity getChargingPostById (String chargingPostId);
    boolean isPaymentIdExists(String chargingPostId);
    boolean addPost(ChargingPostEntity post);
    boolean updatePost(ChargingPostEntity post);
}
