package charging_manage_be.services.report;

import charging_manage_be.model.dto.reports.ReportRequestDTO;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.model.entity.report.ReportEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.cars.CarRepository;
import charging_manage_be.repository.charging_type.ChargingTypeRepository;
import charging_manage_be.repository.report.ReportRepository;
import charging_manage_be.repository.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static charging_manage_be.util.RandomId.generateRandomId;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private ChargingTypeRepository chargingTypeRepository;

    private int characterLength = 5;
    private int numberLength = 5;

    public String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (carRepository.existsById(newId));
        return newId;
    }

    @Override
    public boolean createReport(ReportRequestDTO reportRequestDTO) {
        // Trong report mà admin xử lý, phải có id chủ cũ và chủ mới dựa trên email mà user gửi cho admin
        UserEntity olderOwner = userRepository.findById(reportRequestDTO.getOlderOwnerId()).orElse(null);
        UserEntity newerOwner = userRepository.findById(reportRequestDTO.getNewerOwnerId()).orElse(null);

        if (olderOwner == null || newerOwner == null) {
            return false;
        }

        // Giờ phải lấy được xe của chủ cũ để so sánh với biển số hoặc số khung xe và xe đó phải đang active

        CarEntity ownerOldCar;

        // 1. THỬ theo số khung trước (TRƯỜNG HỢP CHUYỂN NHƯỢNG)
        ownerOldCar = carRepository.findByChassisNumberAndUserAndIsActiveTrue(reportRequestDTO.getChassisNumber(), olderOwner);

        // 2. Nếu không tìm thấy → thử theo biển số (TRƯỜNG HỢP BIỂN GIẢ)
        if (ownerOldCar == null) {ownerOldCar = carRepository.findByLicensePlateAndUserAndIsActiveTrue(reportRequestDTO.getLicensePlate(), olderOwner);
        }

        if (ownerOldCar == null) {
            return false;
        }
        // Sau đó phải unActive trạng thái của xe cũ
        ownerOldCar.setIsActive(false);
        carRepository.save(ownerOldCar);


        // Tạo xe mới với chủ mới và các thông tin giống hệt xe cũ ngoại trừ biển số xe và người dùng
        CarEntity newCar = new CarEntity();
        newCar.setCarID(generateUniqueId());
        newCar.setLicensePlate(reportRequestDTO.getLicensePlate());
        newCar.setUser(newerOwner);
        newCar.setTypeCar(reportRequestDTO.getTypeCar());
        newCar.setChassisNumber(reportRequestDTO.getChassisNumber());
        ChargingTypeEntity chargingTypeEntity = chargingTypeRepository.findById(reportRequestDTO.getChargeType()).orElse(null);
        newCar.setChargingType(chargingTypeEntity);
        newCar.setIsActive(true);
        carRepository.save(newCar);


        // Tạo report để lưu lại lịch sử chuyển đổi
        ReportEntity newReport = new ReportEntity();
        newReport.setReportId(generateUniqueId());
        newReport.setCar(newCar);
        newReport.setOlderOwnerId(olderOwner.getUserID());
        newReport.setNewerOwnerId(newerOwner.getUserID());
        newReport.setTitle(reportRequestDTO.getTitle());
        newReport.setContent(reportRequestDTO.getContent());
        reportRepository.save(newReport);
        return true;
    }

    @Override
    public ReportEntity getReportById(String reportId) {
        return null;
    }

    @Override
    public List<ReportEntity> getAllReports() {
        return List.of();
    }
}
