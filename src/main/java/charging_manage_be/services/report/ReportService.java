package charging_manage_be.services.report;

import charging_manage_be.model.dto.reports.ReportRequestDTO;
import charging_manage_be.model.entity.report.ReportEntity;

import java.util.List;

public interface ReportService {
    boolean createReport(ReportRequestDTO reportRequestDTO);
    ReportEntity getReportById(String reportId);
    List<ReportEntity> getAllReports();

}
