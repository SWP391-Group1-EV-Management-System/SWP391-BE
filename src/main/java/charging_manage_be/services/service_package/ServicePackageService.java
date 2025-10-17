package charging_manage_be.services.service_package;

import charging_manage_be.model.dto.service_package.ServicePackageRequestDTO;
import charging_manage_be.model.dto.service_package.ServicePackageResponseDTO;
import charging_manage_be.model.entity.service_package.ServicePackageEntity;

import java.util.List;

public interface ServicePackageService{
    boolean createServicePackage(ServicePackageRequestDTO servicePackageRequestDTO);
    boolean updateServicePackage(String packageId, ServicePackageRequestDTO servicePackageRequestDTO);
    boolean deleteServicePackage(String packageId);
    List<ServicePackageEntity> getServicePackages();
    ServicePackageResponseDTO getServicePackageById(String packageId);

}
