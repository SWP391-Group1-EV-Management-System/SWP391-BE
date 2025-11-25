package charging_manage_be.controller.reports;

import charging_manage_be.model.dto.reports.ReportRequestDTO;
import charging_manage_be.model.dto.reports.ReportResponseDTO;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.report.ReportEntity;
import charging_manage_be.services.report.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/create")
    public ResponseEntity<String> createReport(@RequestBody ReportRequestDTO reportRequestDTO) {
        boolean isCreated = reportService.createReport(reportRequestDTO);
        if (isCreated) {
            return ResponseEntity.ok("Report created successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to create report. Please check the provided information.");
        }
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponseDTO> getReportById(@PathVariable String reportId) {
        ReportEntity report = reportService.getReportById(reportId);
        ReportResponseDTO reportResponseDTO = new ReportResponseDTO();

        reportResponseDTO.setTitle(report.getTitle());
        reportResponseDTO.setContent(report.getContent());
        reportResponseDTO.setReportId(report.getReportId());
        reportResponseDTO.setOlderOwnerId(report.getOlderOwnerId());
        reportResponseDTO.setNewerOwnerId(report.getNewerOwnerId());
        reportResponseDTO.setLicensePlate(report.getCar().getLicensePlate());
        reportResponseDTO.setTypeCar(report.getCar().getTypeCar());
        reportResponseDTO.setChassisNumber(report.getCar().getChassisNumber());
        reportResponseDTO.setChargeType(report.getCar().getChargingType().getIdChargingType());

        if (report != null) {
            return ResponseEntity.ok(reportResponseDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReportResponseDTO>> getAllReports() {
        List<ReportEntity> reports = reportService.getAllReports();
        List<ReportResponseDTO> reportResponseDTOs = new ArrayList<>();
        for (ReportEntity report : reports) {
            ReportResponseDTO dto = new ReportResponseDTO();
            dto.setReportId(report.getReportId());
            dto.setOlderOwnerId(report.getOlderOwnerId());
            dto.setNewerOwnerId(report.getNewerOwnerId());
            dto.setTitle(report.getTitle());
            dto.setContent(report.getContent());
            CarEntity car = report.getCar();
            dto.setLicensePlate(car.getLicensePlate());
            dto.setTypeCar(car.getTypeCar());
            dto.setChassisNumber(car.getChassisNumber());
            dto.setChargeType(car.getChargingType().getIdChargingType());
            reportResponseDTOs.add(dto);
        }
        return ResponseEntity.ok(reportResponseDTOs);
    }

}
