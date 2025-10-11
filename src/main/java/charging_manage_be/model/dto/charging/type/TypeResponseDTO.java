package charging_manage_be.model.dto.charging.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypeResponseDTO {

    private int idChargingType;
    private String nameChargingType;
    private String description;

}
