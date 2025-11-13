package charging_manage_be.controller.reputations;

import charging_manage_be.model.dto.reputation.ReputationRequest;
import charging_manage_be.model.entity.reputations.ReputationLevelEntity;
import charging_manage_be.services.reputations.ReputationLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reputation-levels")
public class ReputationLevelController {

    @Autowired
    private ReputationLevelService reputationLevelService;

    //Create reputation level
    @PostMapping("/add")
    public ResponseEntity<ReputationLevelEntity> createReputationLevel( @RequestBody ReputationRequest reputationR) {
        ReputationLevelEntity savedLevel = reputationLevelService.saveReputationLevel(reputationR);
        return ResponseEntity.ok(savedLevel);
    }

    // Update reputation level
    @PostMapping("/update/{levelId}")
    public ResponseEntity<ReputationLevelEntity> updateReputationLevel(@PathVariable int levelId, @RequestBody ReputationRequest levelR) {
        try {
            ReputationLevelEntity updatedLevel = reputationLevelService.updateReputationLevel(levelId, levelR);
            return ResponseEntity.ok(updatedLevel);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }

    }

    // Delete reputation level
    @DeleteMapping("/delete/{levelId}")
    public ResponseEntity<String> deleteReputationLevel(@PathVariable int levelId) {
        boolean deleted = reputationLevelService.deleteReputationLevelById(levelId);
        if (deleted) {
            return ResponseEntity.ok("Reputation level deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    // Get reputation level by ID
    @GetMapping("/{levelId}")
        public  ResponseEntity<ReputationLevelEntity> getReputationLevelById(@PathVariable int levelId) {
            Optional<ReputationLevelEntity> level = reputationLevelService.getReputationLevelById(levelId);
            if (level.isPresent()) {
                return ResponseEntity.ok(level.get());
            }
            return ResponseEntity.notFound().build();
        }

    // Get all reputation levels
    @GetMapping("all")
    public ResponseEntity<List<ReputationLevelEntity>> getAllReputationLevels() {
        List<ReputationLevelEntity> levels = reputationLevelService.getAllReputationLevels();
        return ResponseEntity.ok(levels);
    }
}





