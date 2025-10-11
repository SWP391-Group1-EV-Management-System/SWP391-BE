package charging_manage_be.services.charging_type;

import charging_manage_be.model.entity.charging.ChargingTypeEntity;

import java.util.List;

public interface ChargingTypeService {
     ChargingTypeEntity addChargingType(ChargingTypeEntity chargingTypeEntity);
     ChargingTypeEntity getChargingTypeById(int typeId);
     ChargingTypeEntity updateChargingType(ChargingTypeEntity chargingTypeEntity);
     List<ChargingTypeEntity> getAllChargingType();
        boolean deleteChargingTypeById(int typeId);
}
