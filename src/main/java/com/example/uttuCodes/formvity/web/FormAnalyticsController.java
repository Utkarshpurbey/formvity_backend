package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.analytics.FormAnalyticsInsightsDto;
import com.example.uttuCodes.formvity.dto.analytics.FormAnalyticsOverviewDto;
import com.example.uttuCodes.formvity.dto.analytics.FormAnalyticsSummaryDto;
import com.example.uttuCodes.formvity.dto.analytics.QuestionAnalyticsDto;
import com.example.uttuCodes.formvity.dto.analytics.SubmissionListItemDto;
import com.example.uttuCodes.formvity.dto.analytics.TimelineBucketDto;
import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import com.example.uttuCodes.formvity.service.FormAnalyticsService;
import com.example.uttuCodes.formvity.service.FormExcelExportService;
import com.example.uttuCodes.formvity.utils.Utils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/workspaces/{workspaceId}/forms/{formId}")
public class FormAnalyticsController {

    private final FormAnalyticsService formAnalyticsService;
    private final FormExcelExportService formExcelExportService;

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<FormAnalyticsOverviewDto>> overview(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId,
            @RequestParam(defaultValue = "7") int days) {
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(formAnalyticsService.getOverview(workspaceId, formId, userId, days)));
    }

    @GetMapping("/analytics/summary")
    public ResponseEntity<ApiResponse<FormAnalyticsSummaryDto>> summary(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId) {
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(formAnalyticsService.getSummary(workspaceId, formId, userId)));
    }

    @GetMapping("/analytics/timeline")
    public ResponseEntity<ApiResponse<List<TimelineBucketDto>>> timeline(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId,
            @RequestParam(defaultValue = "7") int days) {
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(formAnalyticsService.getTimeline(workspaceId, formId, userId, days)));
    }

    @GetMapping("/analytics/questions")
    public ResponseEntity<ApiResponse<List<QuestionAnalyticsDto>>> questions(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId,
            @RequestParam(defaultValue = "7") int days) {
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(formAnalyticsService.getQuestionBreakdown(workspaceId, formId, userId, days)));
    }

    @GetMapping("/analytics/insights")
    public ResponseEntity<ApiResponse<FormAnalyticsInsightsDto>> insights(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId,
            @RequestParam(defaultValue = "7") int days) {
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(formAnalyticsService.getInsights(workspaceId, formId, userId, days)));
    }

    @GetMapping("/submissions")
    public ResponseEntity<ApiResponse<Page<SubmissionListItemDto>>> submissions(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(formAnalyticsService.listSubmissions(workspaceId, formId, userId, page, size)));
    }

    @GetMapping(value = {"/export/excel", "/export/xlsx", "/submissions/export/excel"}, produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportSubmissionsExcel(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId) {
        UUID userId = Utils.getLoggedInUserId();
        byte[] excelBytes = formExcelExportService.exportSubmissionsToExcel(workspaceId, formId, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        headers.setContentDispositionFormData("attachment", "submissions-export.xlsx");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }
}



