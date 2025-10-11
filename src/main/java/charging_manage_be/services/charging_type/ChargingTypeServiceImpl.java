package charging_manage_be.services.charging_type;

import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.repository.charging_type.ChargingTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChargingTypeServiceImpl implements  ChargingTypeService {
    @Autowired
    private ChargingTypeRepository chargingTypeRepository;


    public ChargingTypeEntity addChargingType(ChargingTypeEntity chargingTypeEntity) {
        return chargingTypeRepository.save(chargingTypeEntity);
    }
    public ChargingTypeEntity getChargingTypeById(int typeId) {
        return chargingTypeRepository.findById(typeId).orElse(null);
    }
    public ChargingTypeEntity updateChargingType(ChargingTypeEntity chargingTypeEntity) {
        return chargingTypeRepository.save(chargingTypeEntity);
    }
    public List<ChargingTypeEntity> getAllChargingType() {
        return chargingTypeRepository.findAll();
    }
    public boolean deleteChargingTypeById(int typeId) {
        if(!chargingTypeRepository.existsById(typeId))
        {
            return false;
        }
        chargingTypeRepository.deleteById(typeId);
        return true;
    }

}
