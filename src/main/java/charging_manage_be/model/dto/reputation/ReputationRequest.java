package charging_manage_be.model.dto.reputation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReputationRequest {
    private String levelName;
    private int minScore;
    private int maxScore;
    private int maxWaitMinutes;
    private String description;
}
