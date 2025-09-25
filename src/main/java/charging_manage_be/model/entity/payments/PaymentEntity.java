package charging_manage_be.model.entity.payments;

import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEntity {
    @Id
    @Column(name = "payment_id")
    private String paymentId; // xử lý trên lớp repo random
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;// khóa ngoại bảng user (người dùng thanh toán)
    @Column(name = "charging_session_id", nullable = false)
    private String chargingSessionId; // khóa ngoại bảng phiên sạc
    @Column (name = "is_paid", nullable = false)
    private boolean isPaid;
    @Column (name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column (name = "paid_at", nullable = true)
    private LocalDateTime paidAt;
    @Column (name = "payment_method", nullable = true)
    private String paymentMethod;

    @Column ( nullable = false)
    private BigDecimal price;
    // tạo payment chỉ bằng 2 trường input vào đó là userId và chargingSessionId
    // lần đầu tạo isPaid sẽ là false và crate createdAt
    // sau khi driver thanh toán update lại isPaid = true và cập nhật paidAt
    @OneToOne
    @JoinColumn(name = "session_id")
    private ChargingSessionEntity session;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isPaid = false;
    }
    @PreUpdate //call save or merge
    protected void onUpdate() {
        this.paidAt = LocalDateTime.now();
    }

}
/*
BigDecimal a = new BigDecimal("10.25");
BigDecimal b = new BigDecimal("0.75");

// Cộng
BigDecimal sum = a.add(b);         // 11.00 (scale thường là 2 nếu cả 2 đều scale 2)

// Trừ
BigDecimal diff = a.subtract(b);   // 9.50

// Nhân
BigDecimal prod = a.multiply(b);   // 7.6875  (scale có thể tăng lên: scale = scale(a)+scale(b))

// Chia -> **cần chú ý**
BigDecimal one = new BigDecimal("1");
BigDecimal three = new BigDecimal("3");
// one.divide(three); // -> BỊ lỗi ArithmeticException do kết quả vô hạn
BigDecimal div = one.divide(three, 10, RoundingMode.HALF_EVEN); // 0.3333333333
 */
