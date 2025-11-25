package charging_manage_be.model.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequestDTO {
    private String reportId;
    private String olderOwnerId;
    private String newerOwnerId;
    private String title;
    private String content;

    private String licensePlate;
    private String typeCar;
    private String chassisNumber;
    private int chargeType;
}
