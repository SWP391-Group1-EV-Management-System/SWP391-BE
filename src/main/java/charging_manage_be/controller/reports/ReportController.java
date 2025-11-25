package charging_manage_be.controller.reports;

import charging_manage_be.model.dto.reports.ReportRequestDTO;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.report.ReportEntity;
import charging_manage_be.services.report.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ReportEntity> getReportById(@PathVariable String reportId) {
        ReportEntity report = reportService.getReportById(reportId);
        if (report != null) {
            return ResponseEntity.ok(report);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReportEntity>> getAllReports() {
        List<ReportEntity> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> countAllReports() {
        int count = reportService.countAllReports();
        return ResponseEntity.ok(count);
    }
}
