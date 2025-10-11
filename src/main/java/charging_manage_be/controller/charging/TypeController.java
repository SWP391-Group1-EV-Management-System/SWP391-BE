package charging_manage_be.controller.charging;

import charging_manage_be.model.dto.charging.type.TypeRequestDTO;
import charging_manage_be.model.dto.charging.type.TypeResponseDTO;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.services.charging_type.ChargingTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charging/type")
public class TypeController {
    @Autowired
    ChargingTypeService chargingTypeService;
    @GetMapping("/all")
    public List<TypeResponseDTO> getAllChargingType() {
        List<TypeResponseDTO> types = chargingTypeService.getAllChargingType().stream().map(
                type -> {
                    TypeResponseDTO dto = new TypeResponseDTO();
                    dto.setIdChargingType(type.getIdChargingType());
                    dto.setNameChargingType(type.getNameChargingType());
                    dto.setDescription(type.getDescription());
                    return dto;
                }).toList();
        return types;
   }
   @GetMapping("/{typeId}")
   public TypeResponseDTO getChargingTypeById(@PathVariable int typeId) {
       ChargingTypeEntity type = chargingTypeService.getChargingTypeById(typeId);
       if (type == null) {
           throw new RuntimeException("Type not found with id: " + typeId);
       }
       else {
           TypeResponseDTO dto = new TypeResponseDTO();
           dto.setIdChargingType(type.getIdChargingType());
           dto.setNameChargingType(type.getNameChargingType());
           dto.setDescription(type.getDescription());
           return dto;
       }
   }
   @DeleteMapping("/{typeId}")
   public String deleteChargingTypeById(@PathVariable int typeId) {
       boolean success = chargingTypeService.deleteChargingTypeById(typeId);

       if (!success) {
           return "Failed to delete type with id: " + typeId;
       }
       return "Delete type completed successfully";
   }
   @PostMapping("/create")
   public String createChargingType(@RequestBody TypeRequestDTO typeRequestDTO) {
       ChargingTypeEntity chargingTypeEntity = new ChargingTypeEntity();
       chargingTypeEntity.setNameChargingType(typeRequestDTO.getNameChargingType());
       chargingTypeEntity.setDescription(typeRequestDTO.getDescription());
       chargingTypeService.addChargingType(chargingTypeEntity);
       return "Create type completed successfully";
   }


}
