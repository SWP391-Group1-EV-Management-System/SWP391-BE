package charging_manage_be.controller.service_package;

import charging_manage_be.model.dto.service_package.PackageTransactionResponseDTO;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.service_package.PackageTransactionEntity;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.service_package.PackageTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/packageTransaction")
public class PackageTransactionController {
    @Autowired
    private PackageTransactionService packageTransactionService;
    @Autowired
    private ChargingSessionService chargingSessionService;

    @PutMapping("/update/status/{packageTransactionId}")
    ResponseEntity<String> updateStatusByPackageTransaction(@PathVariable String packageTransactionId) {
        PackageTransactionResponseDTO packageTransactionResponseDTO = packageTransactionService.getPackageTransactionByPackageTransactionId(packageTransactionId);
        if (packageTransactionResponseDTO == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Package Transaction not found");
        }
        packageTransactionService.updateStatusPackageTransaction(packageTransactionId);
        return ResponseEntity.ok().body("success");
    }

    @GetMapping("/user/{userId}")
    ResponseEntity<List<PackageTransactionResponseDTO>> listPackageTransactionByUserId(@PathVariable String userId) {
        List<PackageTransactionResponseDTO> packageTransactionResponseDTOList = packageTransactionService.getPackageTransactionByUserId(userId);
        if (packageTransactionResponseDTOList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Package Transaction not found");
        }
        return ResponseEntity.ok().body(packageTransactionResponseDTOList);
    }

    @GetMapping("/user/current/{userId}")
    ResponseEntity<PackageTransactionResponseDTO> listPackageTransactionByUserIdCurrent(@PathVariable String userId) {
        PackageTransactionResponseDTO packageTransactionResponseDTOList = packageTransactionService.getLatestActivePackageByUserId(userId);
        if (packageTransactionResponseDTOList == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Package Transaction not found");
        }
        return ResponseEntity.ok().body(packageTransactionResponseDTOList);
    }

    @GetMapping("/{packageTransactionId}")
    ResponseEntity<PackageTransactionResponseDTO> getPackageTransactionByPackageTransactionId(@PathVariable String packageTransactionId) {
        PackageTransactionResponseDTO packageTransactionResponseDTO = packageTransactionService.getPackageTransactionByPackageTransactionId(packageTransactionId);
        if (packageTransactionResponseDTO == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Package Transaction not found");
        }
        return ResponseEntity.ok().body(packageTransactionResponseDTO);
    }


    @DeleteMapping("/cancel/{packageTransactionId}")
    public ResponseEntity<Void> cancelPackageTransactionById(@PathVariable String packageTransactionId) {

        if (packageTransactionService.getPackageTransactionByPackageTransactionId(packageTransactionId) == null) {
            return ResponseEntity.badRequest().build();
        } else {
            packageTransactionService.deletePackageTransactionById(packageTransactionId);
            return ResponseEntity.ok().build();
        }

    }


}