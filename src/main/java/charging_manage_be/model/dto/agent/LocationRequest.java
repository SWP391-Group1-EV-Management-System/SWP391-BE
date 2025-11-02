package charging_manage_be.model.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationRequest {
    private Double latitude;
    private Double longitude;
    private Double radiusKm = 10.0; // Bán kính tìm kiếm mặc định 10km
    private Integer limit = 5; // Số lượng kết quả trả về
}