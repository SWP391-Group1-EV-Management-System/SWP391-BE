package charging_manage_be.controller.reputations;

import charging_manage_be.model.entity.reputations.UserReputationEntity;
import charging_manage_be.services.user_reputations.UserReputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user-reputations")
public class UserReputationController {

    @Autowired
    private UserReputationService userReputationService;


    // Create user reputation
    @PostMapping
    public ResponseEntity<UserReputationEntity> createUserReputation( @RequestBody UserReputationEntity userReputation) {
        UserReputationEntity savedReputation = userReputationService.saveUserReputation(userReputation);
        return ResponseEntity.ok(savedReputation);
    }


    // Get user reputation by user ID
    @GetMapping("/{userID}")
    public ResponseEntity<List<UserReputationEntity>> getUserReputationById(@PathVariable String userID) {
        List<UserReputationEntity> reputations = userReputationService.getUserReputationById(userID);
        if (reputations.isEmpty()) {
            return ResponseEntity.notFound().build(); // notFound() là trả về mã 404 và build() để xây dựng phản hồi
        }
        return ResponseEntity.ok(reputations); // ok là trả về mã 200 và reputations là dữ liệu trả về
    }

    // Get current user reputation by user ID
    @GetMapping("/current/{userID}")
    public ResponseEntity<UserReputationEntity> getCurrentUserReputationByUserId(@PathVariable String userID) {
        Optional<UserReputationEntity> reputation = userReputationService.getCurrentUserReputationById(userID);
        if (reputation.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reputation.get());
    }

    // Get all user reputations
    @GetMapping
    public ResponseEntity<List<UserReputationEntity>> getAllUserReputations() {
        List<UserReputationEntity> reputations = userReputationService.getAllUserReputations();
        return ResponseEntity.ok(reputations);
    }



}
