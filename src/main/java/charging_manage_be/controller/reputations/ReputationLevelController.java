package charging_manage_be.controller.reputations;

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
    @PostMapping
    public ResponseEntity<ReputationLevelEntity> createReputationLevel( @RequestBody ReputationLevelEntity reputationLevel) {
        ReputationLevelEntity savedLevel = reputationLevelService.saveReputationLevel(reputationLevel);
        return ResponseEntity.ok(savedLevel);
    }

    // Update reputation level
    @PutMapping("/{levelID}")
    public ResponseEntity<ReputationLevelEntity> updateReputationLevel(@PathVariable int levelID, @RequestBody ReputationLevelEntity levelDetails) {
        try {
            levelDetails.setLevelID(levelID);
            ReputationLevelEntity updatedLevel = reputationLevelService.updateReputationLevel(levelDetails);
            return ResponseEntity.ok(updatedLevel);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }

    }

    // Delete reputation level
    @DeleteMapping("/{levelID}")
    public ResponseEntity<String> deleteReputationLevel(@PathVariable int levelID) {
        boolean deleted = reputationLevelService.deleteReputationLevelById(levelID);
        if (deleted == true) {
            return ResponseEntity.ok("Reputation level deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    // Get reputation level by ID
    @GetMapping("/{levelID}")
        public  ResponseEntity<ReputationLevelEntity> getReputationLevelById(@PathVariable int levelID) {
            Optional<ReputationLevelEntity> level = reputationLevelService.getReputationLevelById(levelID);
            if (level.isPresent()) {
                return ResponseEntity.ok(level.get());
            }
            return ResponseEntity.notFound().build();
        }

    // Get all reputation levels
    @GetMapping
    public ResponseEntity<List<ReputationLevelEntity>> getAllReputationLevels() {
        List<ReputationLevelEntity> levels = reputationLevelService.getAllReputationLevels();
        return ResponseEntity.ok(levels);
    }
}





