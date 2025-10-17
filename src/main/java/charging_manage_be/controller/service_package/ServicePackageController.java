package charging_manage_be.controller.service_package;

import charging_manage_be.model.dto.service_package.ServicePackageRequestDTO;
import charging_manage_be.model.dto.service_package.ServicePackageResponseDTO;
import charging_manage_be.services.service_package.ServicePackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/servicePackage")
public class ServicePackageController {

    @Autowired
    private ServicePackageService servicePackageService;

    @PostMapping("/create")
    ResponseEntity<String> createServicePackage(@RequestBody ServicePackageRequestDTO servicePackageRequestDTO) {
        if (servicePackageRequestDTO == null) {
            return ResponseEntity.badRequest().build();
        }
        else{
            servicePackageService.createServicePackage(servicePackageRequestDTO);
            return ResponseEntity.ok().build();
        }
    }

    @PutMapping("/update/{servicePackageId}")
    ResponseEntity<String> updateServicePackage(@PathVariable String servicePackageId, @RequestBody ServicePackageRequestDTO servicePackageRequestDTO) {
        ServicePackageResponseDTO findPackage = servicePackageService.getServicePackageById(servicePackageId);
        if (findPackage == null) {
            return ResponseEntity.badRequest().build();
        }
        else{
            servicePackageService.updateServicePackage(servicePackageId, servicePackageRequestDTO);
            return ResponseEntity.ok().build();
        }
    }

    @DeleteMapping("/delete/{servicePackageId}")
    ResponseEntity<String> deleteServicePackage(@PathVariable String servicePackageId) {
        if (servicePackageService.getServicePackageById(servicePackageId) == null) {
            return ResponseEntity.badRequest().build();
        }
        else{
            servicePackageService.deleteServicePackage(servicePackageId);
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping("/getPackageById/{servicePackageId}")
    ResponseEntity<ServicePackageResponseDTO> getServicePackageById(@PathVariable String servicePackageId) {
        if (servicePackageService.getServicePackageById(servicePackageId) == null) {
            return ResponseEntity.badRequest().build();
        }
        else{
            ServicePackageResponseDTO result = servicePackageService.getServicePackageById(servicePackageId);
            return ResponseEntity.ok(result);
        }
    }
}
