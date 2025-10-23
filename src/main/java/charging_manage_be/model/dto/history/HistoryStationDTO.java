package charging_manage_be.model.dto.history;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryStationDTO {
    private String id;
    private String name;
    private String address;
}
/*
"station": {
    "name": "Trạm Sạc FPT Hòa Lạc",
    "address": "Km29, Đại lộ Thăng Long, Hòa Lạc, Hà Nội"
  },
 */