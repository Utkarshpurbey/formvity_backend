package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.entity.FormEntity;
import com.example.uttuCodes.formvity.entity.SubmissionEntity;

import java.util.List;
import java.util.Map;

record AnalyticsContext(
        FormEntity form,
        List<SubmissionEntity> submissions,
        List<Map<String, Object>> fieldDefs,
        int windowDays) {}
