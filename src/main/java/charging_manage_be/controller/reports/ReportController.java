package charging_manage_be.controller.reports;

import charging_manage_be.model.dto.reports.ReportRequestDTO;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.report.ReportEntity;
import charging_manage_be.services.report.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
