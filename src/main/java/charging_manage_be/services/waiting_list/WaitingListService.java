package charging_manage_be.services.waiting_list;


import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.cars.CarEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface WaitingListService{
    // Đối với WaitingListService, ta chỉ cần các phương thức: Thêm, thay đổi status dựa theo userID trên cùng một vòng đời của 1 record
    // Có nghĩa là từ lúc vừa vào danh sách chờ đến lúc rời khỏi danh sách chờ (vào được bãi sạc hoặc hủy chờ)
    // Và các hàm lấy thông tin danh sách của userID với thời gian mới nhất

    WaitingListEntity addToWaitingList(String userID, String chargingPostId,String carID);
   // Đây là hàm khi hủy chờ, tức là userID sẽ bị xóa khỏi danh sách chờ trong Redis và status của record trong DB sẽ được cập nhật thành "CANCELED"
    void cancelWaiting(String waitingListId);
    List<WaitingListEntity> getWaitingListForPost(String chargingPostID);
    List<WaitingListEntity> getWaitingListForStation(String chargingStationID);
    List<WaitingListEntity> getWaitingListForUser(String userID);
    WaitingListEntity getWaitingListForWaitingListId(String waitingListId);
    List<WaitingListEntity> getWaitingListForDate(LocalDateTime startOfDay, LocalDateTime endOfDay);
    boolean addExpectedWaitingTime(String postId, LocalDateTime expectedWaitingTime);
}
