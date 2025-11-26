package charging_manage_be.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {
    private String firstName;
    private String lastName;
    private boolean gender;
    private String phone;
}
