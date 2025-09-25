package charging_manage_be.repository.charging_station;

import charging_manage_be.model.entity.charging.ChargingStationEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


public class ChargingStationRepositoryImpl {
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public boolean addStation(ChargingStationEntity station) {
        try {
            entityManager.persist(station);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Transactional
    public boolean updateStation(ChargingStationEntity station) {
        try {
            entityManager.merge(station);
            return true;
        } catch (Exception e) {
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
