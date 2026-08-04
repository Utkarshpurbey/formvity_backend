package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.entity.FormEntity;
import com.example.uttuCodes.formvity.entity.FormPublicationEntity;
import com.example.uttuCodes.formvity.entity.SubmissionEntity;
import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.FormPublicationRepository;
import com.example.uttuCodes.formvity.repository.FormRepository;
import com.example.uttuCodes.formvity.repository.SubmissionRepository;
import com.example.uttuCodes.formvity.service.FormExcelExportService;
import com.example.uttuCodes.formvity.utils.WorkspaceAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormExcelExportServiceImpl implements FormExcelExportService {

    private final FormRepository formRepository;
    private final FormPublicationRepository formPublicationRepository;
    private final SubmissionRepository submissionRepository;
    private final WorkspaceAccessService workspaceAccessService;

    @Override
    public byte[] exportSubmissionsToExcel(UUID workspaceId, UUID formId, UUID userId) {
        workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);
        FormEntity form = formRepository.findByIdAndWorkspace_WorkSpaceId(formId, workspaceId)
                .orElseThrow(() -> FormvityException.notFound("Form not found: " + formId));

        List<SubmissionEntity> submissions = submissionRepository.findAllWithPublicationByFormId(form.getId());
        List<Map<String, Object>> fields = resolveFieldDefs(form, submissions);
        List<String> respondentKeys = extractRespondentKeys(submissions);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Submissions");

            // Header Style
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}));
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 15, (byte) 23, (byte) 42})); // Dark Slate #0F172A
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.LEFT);

            // Data Style
            XSSFCellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.LEFT);

            // Header Row
            XSSFRow headerRow = sheet.createRow(0);
            createHeaderCell(headerRow, 0, "#", headerStyle);
            createHeaderCell(headerRow, 1, "Submitted At", headerStyle);

            int colIndex = 2;
            // Dynamic Respondent Detail Columns
            for (String respKey : respondentKeys) {
                createHeaderCell(headerRow, colIndex++, respKey, headerStyle);
            }

            // Dynamic Question Columns
            for (Map<String, Object> field : fields) {
                String id = FormAnalyticsSupport.fieldId(field);
                String label = FormAnalyticsSupport.fieldLabel(field);

                // If label equals id or is missing, try to resolve descriptive title from submission answers
                if ((label == null || label.equals(id)) && id != null) {
                    String titleFromAnswers = findTitleInAnswers(id, submissions);
                    if (titleFromAnswers != null) {
                        label = titleFromAnswers;
                    }
                }

                createHeaderCell(headerRow, colIndex++, label != null ? label : (id != null ? id : "Question"), headerStyle);
            }

            // Data Rows
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            int rowIndex = 1;
            for (SubmissionEntity s : submissions) {
                XSSFRow row = sheet.createRow(rowIndex);

                // Col 0: Index
                createCell(row, 0, String.valueOf(rowIndex), dataStyle);

                // Col 1: Submitted At
                String timeStr = s.getCreatedAt() != null ? s.getCreatedAt().format(dtf) : "";
                createCell(row, 1, timeStr, dataStyle);

                int dataColIndex = 2;
                // Respondent Details per Column
                Map<String, Object> respondent = s.getRespondent();
                for (String respKey : respondentKeys) {
                    String respVal = "";
                    if (respondent != null && respondent.containsKey(respKey)) {
                        respVal = extractAnswerValue(respondent.get(respKey));
                    }
                    createCell(row, dataColIndex++, respVal, dataStyle);
                }

                // Answers per Question Column
                Map<String, Object> answers = s.getAnswers();
                for (Map<String, Object> field : fields) {
                    String fieldId = FormAnalyticsSupport.fieldId(field);
                    String answerVal = "";
                    if (fieldId != null && answers != null && answers.containsKey(fieldId)) {
                        Object rawVal = answers.get(fieldId);
                        answerVal = extractAnswerValue(rawVal);
                    }
                    createCell(row, dataColIndex++, answerVal, dataStyle);
                }

                rowIndex++;
            }

            // Auto-size columns with safety limits
            int totalCols = 2 + respondentKeys.size() + fields.size();
            for (int i = 0; i < totalCols; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth < 3000) {
                    sheet.setColumnWidth(i, 3000);
                } else if (currentWidth > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate Excel export for form {}", formId, e);
            throw FormvityException.internalServerError("Failed to generate Excel export: " + e.getMessage());
        }
    }

    private List<String> extractRespondentKeys(List<SubmissionEntity> submissions) {
        Map<String, Boolean> keysMap = new LinkedHashMap<>();
        for (SubmissionEntity s : submissions) {
            Map<String, Object> respondent = s.getRespondent();
            if (respondent != null) {
                for (String key : respondent.keySet()) {
                    if (key != null && !key.isBlank()) {
                        keysMap.putIfAbsent(key, true);
                    }
                }
            }
        }
        return new ArrayList<>(keysMap.keySet());
    }

    private String extractAnswerValue(Object rawVal) {
        if (rawVal == null || FormAnalyticsSupport.isBlankAnswer(rawVal)) {
            return "";
        }
        if (rawVal instanceof Map<?, ?> mapVal) {
            Object valObj = mapVal.get("value");
            if (valObj == null) {
                valObj = mapVal.get("val");
            }
            if (valObj == null) {
                valObj = mapVal.get("text");
            }
            if (valObj == null) {
                valObj = mapVal.get("label");
            }
            if (valObj != null) {
                return extractAnswerValue(valObj);
            }
            // Fallback: join non-title entries
            StringBuilder sb = new StringBuilder();
            mapVal.forEach((k, v) -> {
                if (v != null && !k.toString().equalsIgnoreCase("title")) {
                    sb.append(v).append("; ");
                }
            });
            return sb.length() > 0 ? sb.toString().replaceAll("; $", "") : "";
        }
        if (rawVal instanceof Collection<?> col) {
            return col.stream()
                    .filter(item -> !FormAnalyticsSupport.isBlankAnswer(item))
                    .map(this::extractAnswerValue)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining(", "));
        }
        return rawVal.toString();
    }

    private String findTitleInAnswers(String fieldId, List<SubmissionEntity> submissions) {
        for (SubmissionEntity s : submissions) {
            Map<String, Object> answers = s.getAnswers();
            if (answers != null && answers.containsKey(fieldId)) {
                Object val = answers.get(fieldId);
                if (val instanceof Map<?, ?> valMap) {
                    Object titleObj = valMap.get("title");
                    if (titleObj == null) {
                        titleObj = valMap.get("label");
                    }
                    if (titleObj != null && !titleObj.toString().isBlank()) {
                        return titleObj.toString().trim();
                    }
                }
            }
        }
        return null;
    }

    private void createHeaderCell(XSSFRow row, int column, String value, XSSFCellStyle style) {
        XSSFCell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createCell(XSSFRow row, int column, String value, XSSFCellStyle style) {
        XSSFCell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private List<Map<String, Object>> resolveFieldDefs(FormEntity form, List<SubmissionEntity> submissions) {
        List<Map<String, Object>> fields = FormAnalyticsSupport.extractFields(resolvePageDef(form));
        if (!fields.isEmpty() || submissions.isEmpty()) {
            return fields;
        }
        return FormAnalyticsSupport.inferFieldsFromAnswers(
                submissions.stream().map(SubmissionEntity::getAnswers).toList());
    }

    private Map<String, Object> resolvePageDef(FormEntity form) {
        return formPublicationRepository.findByForm_IdAndCurrentTrue(form.getId())
                .map(FormPublicationEntity::getPublishedPageDef)
                .filter(def -> def != null && !def.isEmpty())
                .orElse(form.getDraftPageDef());
    }
}
