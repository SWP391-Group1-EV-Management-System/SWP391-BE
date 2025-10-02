package charging_manage_be.services.charging_type;

import charging_manage_be.model.entity.charging.ChargingTypeEntity;

public interface ChargingTypeService {
     ChargingTypeEntity addChargingType(ChargingTypeEntity chargingTypeEntity);
     ChargingTypeEntity getChargingTypeById(int typeId);
     ChargingTypeEntity updateChargingType(ChargingTypeEntity chargingTypeEntity);

}
