package charging_manage_be.model.dto.history;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryPaymentDTO {
    private String id;
    private boolean isPaid;
    private String methodId;
    private String methodName;
    private LocalDateTime paidAt;
}
