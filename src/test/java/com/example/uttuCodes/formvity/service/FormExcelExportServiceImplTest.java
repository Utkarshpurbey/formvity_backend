package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.entity.FormEntity;
import com.example.uttuCodes.formvity.entity.SubmissionEntity;
import com.example.uttuCodes.formvity.repository.FormPublicationRepository;
import com.example.uttuCodes.formvity.repository.FormRepository;
import com.example.uttuCodes.formvity.repository.SubmissionRepository;
import com.example.uttuCodes.formvity.service.impl.FormExcelExportServiceImpl;
import com.example.uttuCodes.formvity.utils.WorkspaceAccessService;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.uttuCodes.formvity.repository.SubmissionTagRepository;

@ExtendWith(MockitoExtension.class)
class FormExcelExportServiceImplTest {

    @Mock
    private FormRepository formRepository;

    @Mock
    private FormPublicationRepository formPublicationRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private SubmissionTagRepository submissionTagRepository;

    @Mock
    private WorkspaceAccessService workspaceAccessService;

    @InjectMocks
    private FormExcelExportServiceImpl formExcelExportService;

    private UUID workspaceId;
    private UUID formId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        formId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void exportSubmissionsToExcel_ShouldReturnValidXlsxBytes() throws Exception {
        FormEntity form = new FormEntity();
        form.setId(formId);
        form.setTitle("Customer Feedback Form");
        form.setDraftPageDef(Map.of("fields", List.of(
                Map.of("id", "q1", "label", "Your Rating", "type", "number"),
                Map.of("id", "q2", "label", "Comments", "type", "text")
        )));

        SubmissionEntity submission = new SubmissionEntity();
        submission.setId(UUID.randomUUID());
        submission.setCreatedAt(LocalDateTime.now());
        submission.setRespondent(Map.of("email", "user@example.com"));
        submission.setAnswers(Map.of("q1", 5, "q2", "Great experience!"));

        when(formRepository.findByIdAndWorkspace_WorkSpaceId(formId, workspaceId)).thenReturn(Optional.of(form));
        when(submissionRepository.findAllWithPublicationByFormId(formId)).thenReturn(List.of(submission));

        byte[] excelBytes = formExcelExportService.exportSubmissionsToExcel(workspaceId, formId, userId);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        assertEquals((byte) 'P', excelBytes[0]);
        assertEquals((byte) 'K', excelBytes[1]);

        try (ByteArrayInputStream in = new ByteArrayInputStream(excelBytes);
             XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            XSSFSheet sheet = workbook.getSheet("Submissions");
            assertNotNull(sheet);

            XSSFRow headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("#", headerRow.getCell(0).getStringCellValue());
            assertEquals("Submitted At", headerRow.getCell(1).getStringCellValue());
            assertEquals("Tags", headerRow.getCell(2).getStringCellValue());
            assertEquals("email", headerRow.getCell(3).getStringCellValue());
            assertEquals("Your Rating", headerRow.getCell(4).getStringCellValue());
            assertEquals("Comments", headerRow.getCell(5).getStringCellValue());

            XSSFRow dataRow = sheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals("1", dataRow.getCell(0).getStringCellValue());
            assertEquals("user@example.com", dataRow.getCell(3).getStringCellValue());
            assertEquals("5", dataRow.getCell(4).getStringCellValue());
            assertEquals("Great experience!", dataRow.getCell(5).getStringCellValue());
        }

        verify(workspaceAccessService).requireUserExistInWorkSpace(userId, workspaceId);
    }

    @Test
    void exportSubmissionsToExcel_ShouldCreateDynamicRespondentColumnsAndUnpackAnswers() throws Exception {
        FormEntity form = new FormEntity();
        form.setId(formId);
        form.setTitle("Event Registration");

        Map<String, Object> respondentMap = new LinkedHashMap<>();
        respondentMap.put("email", "ewqq@gmail.com");
        respondentMap.put("fullName", "123das");

        Map<String, Object> answersMap = new LinkedHashMap<>();
        answersMap.put("ticketType", Map.of("title", "Ticket Type", "value", "VIP"));
        answersMap.put("attendeeName", Map.of("title", "Attendee Name", "value", "Utkarsh Purbey"));
        answersMap.put("organization", Map.of("title", "Organization", "value", "Formvity"));

        SubmissionEntity submission = new SubmissionEntity();
        submission.setId(UUID.randomUUID());
        submission.setCreatedAt(LocalDateTime.now());
        submission.setRespondent(respondentMap);
        submission.setAnswers(answersMap);

        when(formRepository.findByIdAndWorkspace_WorkSpaceId(formId, workspaceId)).thenReturn(Optional.of(form));
        when(submissionRepository.findAllWithPublicationByFormId(formId)).thenReturn(List.of(submission));

        byte[] excelBytes = formExcelExportService.exportSubmissionsToExcel(workspaceId, formId, userId);

        try (ByteArrayInputStream in = new ByteArrayInputStream(excelBytes);
             XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            XSSFSheet sheet = workbook.getSheet("Submissions");
            assertNotNull(sheet);

            XSSFRow headerRow = sheet.getRow(0);
            assertNotNull(headerRow);

            // Columns: # (0), Submitted At (1), Tags (2), email (3), fullName (4), Ticket Type (5), Attendee Name (6), Organization (7)
            assertEquals("Tags", headerRow.getCell(2).getStringCellValue());
            assertEquals("email", headerRow.getCell(3).getStringCellValue());
            assertEquals("fullName", headerRow.getCell(4).getStringCellValue());
            assertEquals("Ticket Type", headerRow.getCell(5).getStringCellValue());
            assertEquals("Attendee Name", headerRow.getCell(6).getStringCellValue());
            assertEquals("Organization", headerRow.getCell(7).getStringCellValue());

            XSSFRow dataRow = sheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals("ewqq@gmail.com", dataRow.getCell(3).getStringCellValue());
            assertEquals("123das", dataRow.getCell(4).getStringCellValue());
            assertEquals("VIP", dataRow.getCell(5).getStringCellValue());
            assertEquals("Utkarsh Purbey", dataRow.getCell(6).getStringCellValue());
            assertEquals("Formvity", dataRow.getCell(7).getStringCellValue());
        }
    }
}
