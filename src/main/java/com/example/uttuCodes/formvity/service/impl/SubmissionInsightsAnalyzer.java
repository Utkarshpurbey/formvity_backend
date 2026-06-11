package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.dto.analytics.AudienceInsightsDto;
import com.example.uttuCodes.formvity.dto.analytics.CompletionInsightsDto;
import com.example.uttuCodes.formvity.dto.analytics.DayOfWeekBucketDto;
import com.example.uttuCodes.formvity.dto.analytics.DimensionInsightDto;
import com.example.uttuCodes.formvity.dto.analytics.FormAnalyticsInsightsDto;
import com.example.uttuCodes.formvity.dto.analytics.HourBucketDto;
import com.example.uttuCodes.formvity.dto.analytics.PublicationVersionInsightDto;
import com.example.uttuCodes.formvity.dto.analytics.QuestionDistributionItemDto;
import com.example.uttuCodes.formvity.dto.analytics.TemporalInsightsDto;
import com.example.uttuCodes.formvity.dto.analytics.TrafficInsightsDto;
import com.example.uttuCodes.formvity.entity.FormPublicationEntity;
import com.example.uttuCodes.formvity.entity.SubmissionEntity;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

final class SubmissionInsightsAnalyzer {

    private static final int TOP_N = 10;
    private static final int MAX_ATTRIBUTE_CARDINALITY = 25;
    private static final Set<String> EMAIL_KEYS = Set.of("email", "e-mail", "emailaddress", "email_address");
    private static final Set<String> NAME_KEYS = Set.of("name", "full_name", "fullname", "displayname", "display_name");
    private static final Set<String> PHONE_KEYS = Set.of("phone", "mobile", "phone_number", "phonenumber", "tel");

    private SubmissionInsightsAnalyzer() {}

    static FormAnalyticsInsightsDto analyze(
            List<SubmissionEntity> submissions,
            List<Map<String, Object>> fieldDefs) {
        if (submissions.isEmpty()) {
            return FormAnalyticsInsightsDto.builder()
                    .windowDays(0)
                    .audience(emptyAudience())
                    .traffic(emptyTraffic())
                    .temporal(emptyTemporal())
                    .completion(emptyCompletion(fieldDefs.size()))
                    .publications(List.of())
                    .build();
        }

        return FormAnalyticsInsightsDto.builder()
                .audience(buildAudience(submissions))
                .traffic(buildTraffic(submissions))
                .temporal(buildTemporal(submissions))
                .completion(buildCompletion(submissions, fieldDefs))
                .publications(buildPublications(submissions))
                .build();
    }

    private static AudienceInsightsDto buildAudience(List<SubmissionEntity> submissions) {
        Map<String, Integer> fingerprintCounts = new HashMap<>();
        long withEmail = 0;
        long withName = 0;
        long withPhone = 0;
        Map<String, Long> emailDomains = new LinkedHashMap<>();
        Map<String, Map<String, Long>> attributeCounts = new LinkedHashMap<>();

        for (SubmissionEntity submission : submissions) {
            Map<String, Object> respondent = submission.getRespondent();
            if (respondent == null) {
                continue;
            }

            String email = findRespondentValue(respondent, EMAIL_KEYS);
            String name = findRespondentValue(respondent, NAME_KEYS);
            String phone = findRespondentValue(respondent, PHONE_KEYS);

            if (email != null) {
                withEmail++;
                String domain = extractEmailDomain(email);
                if (domain != null) {
                    emailDomains.merge(domain, 1L, Long::sum);
                }
            }
            if (name != null) {
                withName++;
            }
            if (phone != null) {
                withPhone++;
            }

            String fingerprint = FormAnalyticsSupport.firstNonBlank(
                    email, phone, name, submission.getId().toString());
            fingerprintCounts.merge(fingerprint, 1, Integer::sum);

            for (Map.Entry<String, Object> entry : respondent.entrySet()) {
                String key = entry.getKey();
                if (key == null || isEmailKey(key)) {
                    continue;
                }
                String value = FormAnalyticsSupport.stringValue(entry.getValue());
                if (value == null || value.length() > 80) {
                    continue;
                }
                attributeCounts
                        .computeIfAbsent(key, ignored -> new LinkedHashMap<>())
                        .merge(value, 1L, Long::sum);
            }
        }

        long unique = fingerprintCounts.size();
        long returning = fingerprintCounts.values().stream().filter(c -> c > 1).count();

        List<DimensionInsightDto> domainInsights = List.of(
                toDimension("email_domain", "Email domains", withEmail, emailDomains));
        List<DimensionInsightDto> attributeInsights = attributeCounts.entrySet().stream()
                .filter(e -> e.getValue().size() <= MAX_ATTRIBUTE_CARDINALITY)
                .sorted(Map.Entry.comparingByKey())
                .map(e -> toDimension(e.getKey(), humanize(e.getKey()), submissions.size(), e.getValue()))
                .toList();

        return AudienceInsightsDto.builder()
                .uniqueRespondents(unique)
                .returningRespondents(returning)
                .withEmail(withEmail)
                .withName(withName)
                .withPhone(withPhone)
                .emailDomains(domainInsights)
                .respondentAttributes(attributeInsights)
                .build();
    }

    private static TrafficInsightsDto buildTraffic(List<SubmissionEntity> submissions) {
        long withMetadata = submissions.stream()
                .filter(s -> s.getMetadata() != null && !s.getMetadata().isEmpty())
                .count();

        Map<String, Map<String, Long>> dimensionCounts = new LinkedHashMap<>();
        registerDimension(dimensionCounts, "browser", submissions, SubmissionInsightsAnalyzer::resolveBrowser);
        registerDimension(dimensionCounts, "operating_system", submissions, SubmissionInsightsAnalyzer::resolveOs);
        registerDimension(dimensionCounts, "device_type", submissions, SubmissionInsightsAnalyzer::resolveDevice);
        registerDimension(dimensionCounts, "referrer", submissions, SubmissionInsightsAnalyzer::resolveReferrer);
        registerDimension(dimensionCounts, "utm_source", submissions, s -> metadataValue(s, "utm_source", "utmSource", "source"));
        registerDimension(dimensionCounts, "utm_medium", submissions, s -> metadataValue(s, "utm_medium", "utmMedium", "medium"));
        registerDimension(dimensionCounts, "utm_campaign", submissions, s -> metadataValue(s, "utm_campaign", "utmCampaign", "campaign"));
        registerDimension(dimensionCounts, "locale", submissions, s -> metadataValue(s, "locale", "language", "lang"));
        registerDimension(dimensionCounts, "timezone", submissions, s -> metadataValue(s, "timezone", "tz", "timeZone"));
        registerDimension(dimensionCounts, "country", submissions, s -> metadataValue(s, "country", "countryCode", "country_code"));
        registerDimension(dimensionCounts, "city", submissions, s -> metadataValue(s, "city"));
        registerDimension(dimensionCounts, "platform", submissions, s -> metadataValue(s, "platform"));

        for (SubmissionEntity submission : submissions) {
            Map<String, Object> metadata = submission.getMetadata();
            if (metadata == null) {
                continue;
            }
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                String key = entry.getKey();
                if (key == null || isReservedMetadataKey(key)) {
                    continue;
                }
                String value = FormAnalyticsSupport.stringValue(entry.getValue());
                if (value == null || value.length() > 120) {
                    continue;
                }
                Map<String, Long> counts = dimensionCounts.computeIfAbsent(
                        "meta_" + key, ignored -> new LinkedHashMap<>());
                if (counts.size() <= MAX_ATTRIBUTE_CARDINALITY) {
                    counts.merge(value, 1L, Long::sum);
                }
            }
        }

        List<DimensionInsightDto> dimensions = dimensionCounts.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(e -> toDimension(e.getKey(), humanize(e.getKey()), submissions.size(), e.getValue()))
                .toList();

        return TrafficInsightsDto.builder()
                .withMetadata(withMetadata)
                .dimensions(dimensions)
                .build();
    }

    private static TemporalInsightsDto buildTemporal(List<SubmissionEntity> submissions) {
        Map<Integer, Long> hourCounts = new LinkedHashMap<>();
        for (int h = 0; h < 24; h++) {
            hourCounts.put(h, 0L);
        }
        Map<DayOfWeek, Long> dayCounts = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            dayCounts.put(day, 0L);
        }

        for (SubmissionEntity submission : submissions) {
            LocalDateTime createdAt = submission.getCreatedAt();
            if (createdAt == null) {
                continue;
            }
            hourCounts.merge(createdAt.getHour(), 1L, Long::sum);
            dayCounts.merge(createdAt.getDayOfWeek(), 1L, Long::sum);
        }

        Integer peakHour = hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .orElse(null);

        String peakDay = dayCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 0)
                .map(e -> e.getKey().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                .orElse(null);

        List<HourBucketDto> hours = hourCounts.entrySet().stream()
                .map(e -> new HourBucketDto(e.getKey(), e.getValue()))
                .toList();
        List<DayOfWeekBucketDto> days = dayCounts.entrySet().stream()
                .map(e -> new DayOfWeekBucketDto(
                        e.getKey().getDisplayName(TextStyle.FULL, Locale.ENGLISH), e.getValue()))
                .toList();

        return TemporalInsightsDto.builder()
                .peakHour(peakHour)
                .peakDayOfWeek(peakDay)
                .byHourOfDay(hours)
                .byDayOfWeek(days)
                .build();
    }

    private static CompletionInsightsDto buildCompletion(
            List<SubmissionEntity> submissions,
            List<Map<String, Object>> fieldDefs) {
        int totalFields = fieldDefs.size();
        if (totalFields == 0) {
            return emptyCompletion(0);
        }

        List<String> fieldIds = fieldDefs.stream()
                .map(FormAnalyticsSupport::fieldId)
                .filter(id -> id != null)
                .toList();

        long fullyCompleted = 0;
        double totalAnswered = 0;

        for (SubmissionEntity submission : submissions) {
            Map<String, Object> answers = submission.getAnswers();
            long answered = fieldIds.stream()
                    .filter(id -> !FormAnalyticsSupport.isBlankAnswer(answers != null ? answers.get(id) : null))
                    .count();
            totalAnswered += answered;
            if (answered == fieldIds.size()) {
                fullyCompleted++;
            }
        }

        long total = submissions.size();
        double avgAnswered = total == 0 ? 0 : FormAnalyticsSupport.roundPercent(totalAnswered / total);
        double avgCompletion = total == 0 ? 0
                : FormAnalyticsSupport.roundPercent(100.0 * totalAnswered / (total * fieldIds.size()));
        double fullyRate = total == 0 ? 0
                : FormAnalyticsSupport.roundPercent(100.0 * fullyCompleted / total);

        return CompletionInsightsDto.builder()
                .totalFields(totalFields)
                .avgFieldsAnswered(avgAnswered)
                .avgCompletionRate(avgCompletion)
                .fullyCompletedCount(fullyCompleted)
                .fullyCompletedRate(fullyRate)
                .build();
    }

    private static List<PublicationVersionInsightDto> buildPublications(List<SubmissionEntity> submissions) {
        Map<Integer, PublicationAccumulator> byVersion = new LinkedHashMap<>();
        long total = submissions.size();

        for (SubmissionEntity submission : submissions) {
            FormPublicationEntity publication = submission.getPublication();
            if (publication == null) {
                continue;
            }
            int version = publication.getVersion();
            PublicationAccumulator acc = byVersion.computeIfAbsent(version, PublicationAccumulator::new);
            acc.responseCount++;
            acc.slug = publication.getSlug();
            acc.publishedAt = publication.getPublishedAt();
            acc.current = publication.isCurrent();
        }

        return byVersion.values().stream()
                .sorted(Comparator.comparingInt(a -> a.version))
                .map(acc -> PublicationVersionInsightDto.builder()
                        .version(acc.version)
                        .slug(acc.slug)
                        .publishedAt(acc.publishedAt)
                        .responseCount(acc.responseCount)
                        .percent(total == 0 ? 0
                                : FormAnalyticsSupport.roundPercent(100.0 * acc.responseCount / total))
                        .current(acc.current)
                        .build())
                .toList();
    }

    private static void registerDimension(
            Map<String, Map<String, Long>> dimensionCounts,
            String dimension,
            List<SubmissionEntity> submissions,
            Function<SubmissionEntity, String> extractor) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (SubmissionEntity submission : submissions) {
            String value = extractor.apply(submission);
            if (value != null) {
                counts.merge(value, 1L, Long::sum);
            }
        }
        if (!counts.isEmpty()) {
            dimensionCounts.put(dimension, counts);
        }
    }

    private static DimensionInsightDto toDimension(
            String dimension, String label, long totalWithValue, Map<String, Long> counts) {
        long covered = counts.values().stream().mapToLong(Long::longValue).sum();
        List<QuestionDistributionItemDto> items = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_N)
                .map(e -> new QuestionDistributionItemDto(
                        e.getKey(),
                        e.getKey(),
                        e.getValue(),
                        covered == 0 ? 0 : FormAnalyticsSupport.roundPercent(100.0 * e.getValue() / covered)))
                .collect(Collectors.toCollection(ArrayList::new));

        return DimensionInsightDto.builder()
                .dimension(dimension)
                .label(label)
                .totalWithValue(covered)
                .breakdown(items)
                .build();
    }

    private static String resolveBrowser(SubmissionEntity submission) {
        String explicit = metadataValue(submission, "browser");
        if (explicit != null) {
            return explicit;
        }
        return detectBrowser(metadataValue(submission, "userAgent", "user_agent", "ua"));
    }

    private static String resolveOs(SubmissionEntity submission) {
        String explicit = metadataValue(submission, "os", "operatingSystem", "operating_system");
        if (explicit != null) {
            return explicit;
        }
        return detectOs(metadataValue(submission, "userAgent", "user_agent", "ua"));
    }

    private static String resolveDevice(SubmissionEntity submission) {
        String explicit = metadataValue(submission, "device", "deviceType", "device_type");
        if (explicit != null) {
            return explicit;
        }
        return detectDevice(metadataValue(submission, "userAgent", "user_agent", "ua"));
    }

    private static String resolveReferrer(SubmissionEntity submission) {
        String referrer = metadataValue(submission, "referrer", "referer", "referrerUrl", "referrer_url");
        if (referrer == null) {
            return null;
        }
        return extractReferrerHost(referrer);
    }

    private static String metadataValue(SubmissionEntity submission, String... keys) {
        Map<String, Object> metadata = submission.getMetadata();
        if (metadata == null) {
            return null;
        }
        for (String key : keys) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                    return FormAnalyticsSupport.stringValue(entry.getValue());
                }
            }
        }
        return null;
    }

    private static String detectBrowser(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("edg/")) {
            return "Edge";
        }
        if (ua.contains("chrome/") && !ua.contains("edg/")) {
            return "Chrome";
        }
        if (ua.contains("firefox/")) {
            return "Firefox";
        }
        if (ua.contains("safari/") && !ua.contains("chrome/")) {
            return "Safari";
        }
        if (ua.contains("opr/") || ua.contains("opera")) {
            return "Opera";
        }
        return "Other";
    }

    private static String detectOs(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("windows")) {
            return "Windows";
        }
        if (ua.contains("mac os") || ua.contains("macintosh")) {
            return "macOS";
        }
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios")) {
            return "iOS";
        }
        if (ua.contains("linux")) {
            return "Linux";
        }
        return "Other";
    }

    private static String detectDevice(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile";
        }
        if (ua.contains("ipad") || ua.contains("tablet")) {
            return "Tablet";
        }
        return "Desktop";
    }

    private static String extractReferrerHost(String referrer) {
        try {
            String normalized = referrer.contains("://") ? referrer : "https://" + referrer;
            URI uri = URI.create(normalized);
            if (uri.getHost() != null) {
                return uri.getHost().replaceFirst("^www\\.", "");
            }
        } catch (Exception ignored) {
            // fall through
        }
        return referrer.length() > 60 ? referrer.substring(0, 60) : referrer;
    }

    private static String extractEmailDomain(String email) {
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return null;
        }
        return email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    private static String findRespondentValue(Map<String, Object> respondent, Set<String> keys) {
        for (Map.Entry<String, Object> entry : respondent.entrySet()) {
            if (entry.getKey() != null && keys.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                return FormAnalyticsSupport.stringValue(entry.getValue());
            }
        }
        return null;
    }

    private static boolean isEmailKey(String key) {
        return EMAIL_KEYS.contains(key.toLowerCase(Locale.ROOT));
    }

    private static boolean isReservedMetadataKey(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        return Set.of(
                "useragent", "user_agent", "ua", "browser", "os", "operatingsystem", "operating_system",
                "device", "devicetype", "device_type", "referrer", "referer", "referrerurl", "referrer_url",
                "utm_source", "utmsource", "utm_medium", "utmmedium", "utm_campaign", "utmcampaign",
                "source", "medium", "campaign", "locale", "language", "lang", "timezone", "tz",
                "country", "countrycode", "country_code", "city", "platform"
        ).contains(lower.replace("_", ""));
    }

    private static String humanize(String key) {
        String cleaned = key.startsWith("meta_") ? key.substring(5) : key;
        cleaned = cleaned.replace('_', ' ');
        if (cleaned.isEmpty()) {
            return key;
        }
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }

    private static AudienceInsightsDto emptyAudience() {
        return AudienceInsightsDto.builder()
                .emailDomains(List.of())
                .respondentAttributes(List.of())
                .build();
    }

    private static TrafficInsightsDto emptyTraffic() {
        return TrafficInsightsDto.builder().dimensions(List.of()).build();
    }

    private static TemporalInsightsDto emptyTemporal() {
        List<HourBucketDto> hours = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            hours.add(new HourBucketDto(h, 0));
        }
        List<DayOfWeekBucketDto> days = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            days.add(new DayOfWeekBucketDto(
                    day.getDisplayName(TextStyle.FULL, Locale.ENGLISH), 0));
        }
        return TemporalInsightsDto.builder()
                .byHourOfDay(hours)
                .byDayOfWeek(days)
                .build();
    }

    private static CompletionInsightsDto emptyCompletion(int totalFields) {
        return CompletionInsightsDto.builder().totalFields(totalFields).build();
    }

    private static final class PublicationAccumulator {
        private final int version;
        private long responseCount;
        private String slug;
        private LocalDateTime publishedAt;
        private boolean current;

        private PublicationAccumulator(int version) {
            this.version = version;
        }
    }
}
