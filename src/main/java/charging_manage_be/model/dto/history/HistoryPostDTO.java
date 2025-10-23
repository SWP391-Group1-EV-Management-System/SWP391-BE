package charging_manage_be.model.dto.history;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryPostDTO {
    private String id;
    private BigDecimal maxPower;
}
