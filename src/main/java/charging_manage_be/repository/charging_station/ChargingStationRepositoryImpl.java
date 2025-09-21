package charging_manage_be.repository.charging_station;

import charging_manage_be.model.entity.charging_station.ChargingStationEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class ChargingStationRepositoryImpl {
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    public ChargingStationRepositoryImpl(String jpaName) {
        this.entityManagerFactory = Persistence.createEntityManagerFactory(jpaName);// lấy cấu hình từ persistence-unit name="JpaName" có tên là  gì tùy chọn
        this.entityManager = entityManagerFactory.createEntityManager();
    }
    public boolean addStation(ChargingStationEntity station) {
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(station);
            entityManager.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateStation(ChargingStationEntity station) {
        try {
            entityManager.getTransaction().begin();
            entityManager.merge(station);
            entityManager.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    public boolean isExistById(String stationId) {
        return entityManager.find(ChargingStationEntity.class, stationId) != null;
    }
    public ChargingStationEntity getStationById(String stationId) {
        return entityManager.find(ChargingStationEntity.class, stationId);
    }

}
