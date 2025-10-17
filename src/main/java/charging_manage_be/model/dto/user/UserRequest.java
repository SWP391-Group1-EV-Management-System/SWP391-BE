package charging_manage_be.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String id;
    private String firstName;
    private String lastName;
    private Date birthDate;
    private boolean gender;
    private String role;
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private boolean status;
}
