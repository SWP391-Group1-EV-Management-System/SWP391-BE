package charging_manage_be.model.entity.report;

import charging_manage_be.model.entity.cars.CarEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "report")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportEntity {
    @Id
    @Column(name = "report_id", nullable = false)
    private String reportId;
    @Column(name = "user_send_id", nullable = false)
    private String olderOwnerId;
    @Column(name = "user_receive_id", nullable = false)
    private String newerOwnerId;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private CarEntity car;

}
