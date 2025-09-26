//package charging_manage_be.services.waiting_list;
//
//
//import charging_manage_be.model.entity.booking.WaitingListEntity;
//import charging_manage_be.model.entity.cars.CarEntity;
//import charging_manage_be.model.entity.charging.ChargingPostEntity;
//import charging_manage_be.model.entity.charging.ChargingStationEntity;
//import charging_manage_be.model.entity.users.UserEntity;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface WaitingListService{
//    // Đối với WaitingListService, ta chỉ cần các phương thức: Thêm, thay đổi status dựa theo userID trên cùng một vòng đời của 1 record
//    // Có nghĩa là từ lúc vừa vào danh sách chờ đến lúc rời khỏi danh sách chờ (vào được bãi sạc hoặc hủy chờ)
//    // Và các hàm lấy thông tin danh sách của userID với thời gian mới nhất
//
//    public boolean addToWaitingList(UserEntity user, ChargingStationEntity chargingStationEntity, ChargingPostEntity chargingPostEntity, CarEntity carEntity);
//    public boolean removeFromWaitingList(String waitingListID); // Hủy chờ --> chuyển status thành "canceled"
//    public boolean successfulEntry(String waitingListID); // Vào được bãi sạc --> chuyển status thành "completed"
//    public Optional<WaitingListEntity> findLatestByUserId(String userID); // Lấy thông tin danh sách chờ mới nhất của userID
//    public List<WaitingListEntity> findAllUserOnWaitingList(); // Lấy tất cả user đang chờ
//
//}
