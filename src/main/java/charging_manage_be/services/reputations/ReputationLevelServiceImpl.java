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
        return reputationLevelRepository.save(reputationLevelEntity);
    }

    @Override
    public boolean deleteReputationLevelById(int levelID) {
        reputationLevelRepository.deleteById(levelID);
        return true;
    }

    @Override
    public Optional<ReputationLevelEntity> getReputationLevelById(int levelID) {
        return reputationLevelRepository.findById(levelID);
    }

    @Override
    public List<ReputationLevelEntity> getAllReputationLevels() {
        return reputationLevelRepository.findAll();
    }
}
