package charging_manage_be.repository.waiting_list;

<<<<<<< HEAD
import charging_manage_be.model.entity.booking.WaitingListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitingListRepository extends JpaRepository<WaitingListEntity, String> {
=======

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.cars.CarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitingListRepository extends JpaRepository<WaitingListEntity, String> {
    Optional<WaitingListEntity> findFirstByChargingPost_IdChargingPostAndStatusOrderByCreatedAtAsc(String chargingPostID, String status);
>>>>>>> 28dd984 (Code about Waiting and Booking Service)
}
