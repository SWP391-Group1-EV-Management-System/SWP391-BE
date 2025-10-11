package charging_manage_be.repository.users;

import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    // Tìm user theo email đã đăng kí hay chưa
    Optional<UserEntity> findByEmail(String email);
//    // Bởi vì softDeleteUser không có sẵn trong JpaRepository nên ta phải tự modi
//    // Câu lệnh SQL này sẽ được thực thi khi ta gọi phương thức softDeleteUser
//
//    @Modifying // Modifying được sử dụng để đánh dấu rằng truy vấn này sẽ thay đổi dữ liệu (INSERT, UPDATE, DELETE)
//    @Transactional // Đảm bảo rằng thao tác này được thực hiện trong một giao dịch
//    @Query("UPDATE UserEntity u SET u.status = false WHERE u.userID = :userID")
//    int softDeleteUser(@Param("userID") String userID); // Param dùng để ánh xạ tham số trong truy vấn với tham số trong phương thức và sẽ thay thế :userID trong truy vấn bằng giá trị của tham số userID khi phương thức được gọi
}
