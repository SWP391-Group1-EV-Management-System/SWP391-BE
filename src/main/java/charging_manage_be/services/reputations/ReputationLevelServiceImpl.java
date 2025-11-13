package charging_manage_be.services.reputations;

import charging_manage_be.model.dto.reputation.ReputationRequest;
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
    /*
        private int minScore;
    private int maxScore;
    private int maxWaitMinutes;
    private String description;
     */
    @Override
    public ReputationLevelEntity saveReputationLevel(ReputationRequest reputationR) {
        ReputationLevelEntity reputationLevelEntity = new ReputationLevelEntity();
        reputationLevelEntity.setLevelName(reputationR.getLevelName());
        reputationLevelEntity.setMaxScore(reputationR.getMaxScore());
        reputationLevelEntity.setMinScore(reputationR.getMinScore());
        reputationLevelEntity.setDescription(reputationR.getDescription());
        reputationLevelEntity.setMaxWaitMinutes(reputationR.getMaxWaitMinutes());
        return reputationLevelRepository.save(reputationLevelEntity);
    }

    @Override
    public ReputationLevelEntity updateReputationLevel(int repuId, ReputationRequest reputationR) {
        ReputationLevelEntity repuO = reputationLevelRepository.findById(repuId).orElse(null);
        if (repuO == null){
            throw new IllegalArgumentException("ReputationLevelEntity with ID " + repuId + " does not exist");
        }
        else {
            repuO.setLevelName(reputationR.getLevelName());
            repuO.setMaxScore(reputationR.getMaxScore());
            repuO.setMinScore(reputationR.getMinScore());
            repuO.setMaxWaitMinutes(reputationR.getMaxWaitMinutes());
            repuO.setDescription(reputationR.getDescription());
            return reputationLevelRepository.save(repuO);
        }
    }

    @Override
    public boolean deleteReputationLevelById(int levelId) {
        reputationLevelRepository.deleteById(levelId);
        return true;
    }

    @Override
    public Optional<ReputationLevelEntity> getReputationLevelById(int levelId) {
        if (!reputationLevelRepository.existsById(levelId)) {
            throw new IllegalArgumentException("ReputationLevelEntity with ID " + levelId + " does not exist");
        }
        return reputationLevelRepository.findById(levelId);
    }

    @Override
    public List<ReputationLevelEntity> getAllReputationLevels() {
        return reputationLevelRepository.findAll();
    }
}
