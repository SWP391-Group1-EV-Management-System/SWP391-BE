package charging_manage_be.repository.waiting_list;

import charging_manage_be.model.entity.booking.WaitingListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitingListRepository extends JpaRepository<WaitingListEntity, String> {
    // sai- hàm này phải lấy userID và check status not compeleted rồi mới lấy waitinglistentity đó

    @Query("SELECT w FROM WaitingListEntity w WHERE w.user.userID = :id AND w.status = 'waiting'")
    WaitingListEntity findWaitingListByUserID(String id);
}
