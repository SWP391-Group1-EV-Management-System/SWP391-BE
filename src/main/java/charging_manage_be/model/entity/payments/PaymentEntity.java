package charging_manage_be.model.entity.payments;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class PaymentEntity {
    private final int characterLength = 4;
    private final int numberLength = 4;
    @Id
    private String paymentId; // xử lý trên lớp repo random
    @Column ( nullable = false)
    private String userId;
    @Column( nullable = false)
    private String chargingSessionId;
    @Column ( nullable = false)
    private boolean isPaid;
    @Column ( nullable = false)
    private LocalDateTime createdAt;
    @Column ( nullable = true)
    private LocalDateTime paidAt;
    @Column ( nullable = false)
    private BigDecimal price;
    // tạo payment chỉ bằng 2 trường input vào đó là userId và chargingSessionId
    // lần đầu tạo isPaid sẽ là false và crate createdAt
    // sau khi driver thanh toán update lại isPaid = true và cập nhật paidAt

    public PaymentEntity() {
    }

    public int getCharacterLength() {
        return characterLength;
    }

    public int getNumberLength() {
        return numberLength;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChargingSessionId() {
        return chargingSessionId;
    }

    public void setChargingSessionId(String chargingSessionId) {
        this.chargingSessionId = chargingSessionId;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
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
