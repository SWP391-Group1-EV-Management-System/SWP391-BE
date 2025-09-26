package charging_manage_be.services.reputations;

import charging_manage_be.model.entity.reputations.ReputationLevelEntity;
import charging_manage_be.repository.reputations.ReputationLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReputationLevelServiceImpl implements ReputationLevelService {

    @Autowired
    private ReputationLevelRepository reputationLevelRepository;

    @Override
    public ReputationLevelEntity saveReputationLevel(ReputationLevelEntity reputationLevelEntity) {
        if (reputationLevelEntity == null) {
            throw new IllegalArgumentException("ReputationLevelEntity cannot be null");
        }
        else if (reputationLevelRepository.existsById(reputationLevelEntity.getLevelID())){
            throw new IllegalArgumentException("ReputationLevelEntity with ID " + reputationLevelEntity.getLevelID() + " already exists");
        }
        else {
            return reputationLevelRepository.save(reputationLevelEntity);
        }
    }

    @Override
    public ReputationLevelEntity updateReputationLevel(ReputationLevelEntity reputationLevelEntity) {
        if (reputationLevelEntity == null) {
            throw new IllegalArgumentException("ReputationLevelEntity cannot be null");
        }
        else if (!reputationLevelRepository.existsById(reputationLevelEntity.getLevelID())){
            throw new IllegalArgumentException("ReputationLevelEntity with ID " + reputationLevelEntity.getLevelID() + " does not exist");
        }
        else {
            return reputationLevelRepository.save(reputationLevelEntity);
        }
    }

    @Override
    public boolean deleteReputationLevelById(int levelID) {
        if (!reputationLevelRepository.existsById(levelID)) {
            throw new IllegalArgumentException("ReputationLevelEntity with ID " + levelID + " does not exist");
        }
        reputationLevelRepository.deleteById(levelID);
        return true;
    }

    @Override
    public Optional<ReputationLevelEntity> getReputationLevelById(int levelID) {
        if (!reputationLevelRepository.existsById(levelID)) {
            throw new IllegalArgumentException("ReputationLevelEntity with ID " + levelID + " does not exist");
        }
        return reputationLevelRepository.findById(levelID);
    }

    @Override
    public List<ReputationLevelEntity> getAllReputationLevels() {
        return reputationLevelRepository.findAll();
    }
}
