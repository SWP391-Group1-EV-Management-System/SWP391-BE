package charging_manage_be.repository.charnging_post;

import charging_manage_be.model.entity.Charging.ChargingPostEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;


public class ChargingPostRepositoryImpl {
    @PersistenceContext
    private EntityManager entityManager;
    public boolean isExistById(String postId) {
        return entityManager.find(ChargingPostEntity.class, postId) != null;
    }
    public ChargingPostEntity getPostById(String postId) {
        return entityManager.find(ChargingPostEntity.class, postId);
    }
    public boolean deletePost(ChargingPostEntity post) {
        try {
            post.setActive(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updatePost(ChargingPostEntity post) {
        try {
            entityManager.merge(post);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean addPost(ChargingPostEntity post) {
        try {
            entityManager.persist(post);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
