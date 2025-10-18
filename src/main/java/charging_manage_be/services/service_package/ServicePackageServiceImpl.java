package charging_manage_be.services.service_package;

import charging_manage_be.model.dto.service_package.ServicePackageRequestDTO;
import charging_manage_be.model.dto.service_package.ServicePackageResponseDTO;
import charging_manage_be.model.entity.service_package.ServicePackageEntity;
import charging_manage_be.repository.service_package.ServicePackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class ServicePackageServiceImpl implements ServicePackageService {

    @Autowired
    private ServicePackageRepository servicePackageRepository;

    private int characterLength = 5;
    private int numberLength = 5;

    public String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (servicePackageRepository.findById(newId).isPresent());
        return newId;
    }

    @Override
    public boolean createServicePackage(ServicePackageRequestDTO servicePackageRequestDTO) {
        if (servicePackageRequestDTO == null) {
            throw new RuntimeException("Service package request DTO is null");
        }

        ServicePackageEntity servicePackageEntity = new ServicePackageEntity();
        servicePackageEntity.setPackageId(generateUniqueId());
        servicePackageEntity.setPackageName(servicePackageRequestDTO.getPackageName());
        servicePackageEntity.setDescription(servicePackageRequestDTO.getDescription());
        servicePackageEntity.setBillingCycle(servicePackageRequestDTO.getBillingCycle());
        servicePackageEntity.setPrice(servicePackageRequestDTO.getPrice());
        servicePackageEntity.setUnit(servicePackageRequestDTO.getUnit());
        servicePackageEntity.setQuota(servicePackageRequestDTO.getQuota());
        servicePackageRepository.save(servicePackageEntity);
        return true;
    }

    @Override
    public boolean updateServicePackage(String packageId, ServicePackageRequestDTO servicePackageRequestDTO) {
        ServicePackageEntity findServicePackageById = servicePackageRepository.findById(packageId).orElse(null);
        if (findServicePackageById == null) {
            return false;
        }
        findServicePackageById.setPackageId(packageId);
        findServicePackageById.setPackageName(servicePackageRequestDTO.getPackageName());
        findServicePackageById.setDescription(servicePackageRequestDTO.getDescription());
        findServicePackageById.setBillingCycle(servicePackageRequestDTO.getBillingCycle());
        findServicePackageById.setPrice(servicePackageRequestDTO.getPrice());
        findServicePackageById.setUnit(servicePackageRequestDTO.getUnit());
        findServicePackageById.setQuota(servicePackageRequestDTO.getQuota());
        servicePackageRepository.save(findServicePackageById);
        return true;
    }

    @Override
    public boolean deleteServicePackage(String packageId) {
        ServicePackageEntity findServicePackageById = servicePackageRepository.findById(packageId).orElse(null);
        if (findServicePackageById == null) {
            throw new RuntimeException("Service package with id " + packageId + " not found");
        }
        else{
            servicePackageRepository.deleteById(packageId);
        }
        return true;
    }

    @Override
    public List<ServicePackageEntity> getServicePackages() {
        return servicePackageRepository.findAll();
    }

    @Override
    public ServicePackageResponseDTO getServicePackageById(String packageId) {
        ServicePackageEntity servicePackageEntity = servicePackageRepository.findById(packageId).orElse(null);
        if (servicePackageEntity == null) {
            throw new RuntimeException("Service package with id " + packageId + " not found");
        }
        else{
            ServicePackageResponseDTO servicePackageResponseDTO = new ServicePackageResponseDTO();

            servicePackageResponseDTO.setPackageId(packageId);
            servicePackageResponseDTO.setPackageName(servicePackageEntity.getPackageName());
            servicePackageResponseDTO.setDescription(servicePackageEntity.getDescription());
            servicePackageResponseDTO.setBillingCycle(servicePackageEntity.getBillingCycle());
            servicePackageResponseDTO.setPrice(servicePackageEntity.getPrice());
            servicePackageResponseDTO.setUnit(servicePackageEntity.getUnit());
            servicePackageResponseDTO.setQuota(servicePackageEntity.getQuota());
            return servicePackageResponseDTO;
        }
    }
}
