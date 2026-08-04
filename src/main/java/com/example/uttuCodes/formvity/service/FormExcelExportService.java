package com.example.uttuCodes.formvity.service;

import java.util.UUID;

public interface FormExcelExportService {
    byte[] exportSubmissionsToExcel(UUID workspaceId, UUID formId, UUID userId);
}
