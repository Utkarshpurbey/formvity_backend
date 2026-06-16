# Graph Report - .  (2026-06-12)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 746 nodes · 1579 edges · 65 communities (29 shown, 36 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 176 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d33da827`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 50|Community 50]]
- [[_COMMUNITY_Community 51|Community 51]]
- [[_COMMUNITY_Community 52|Community 52]]
- [[_COMMUNITY_Community 53|Community 53]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 57|Community 57]]
- [[_COMMUNITY_Community 58|Community 58]]
- [[_COMMUNITY_Community 59|Community 59]]
- [[_COMMUNITY_Community 60|Community 60]]
- [[_COMMUNITY_Community 61|Community 61]]

## God Nodes (most connected - your core abstractions)
1. `SubmissionInsightsAnalyzer` - 28 edges
2. `String` - 18 edges
3. `GlobalExceptionHandler` - 17 edges
4. `ErrorResponse` - 17 edges
5. `FormAnalyticsServiceImpl` - 17 edges
6. `ResponseEntity` - 16 edges
7. `ExceptionHandler` - 16 edges
8. `FormAnalyticsSupport` - 16 edges
9. `SubmissionEntity` - 13 edges
10. `FormvityException` - 12 edges

## Surprising Connections (you probably didn't know these)
- `SecurityConfig` --references--> `JWT Authentication`  [INFERRED]
  docs/RBAC.md → README.md
- `formvity-api Web Service` --uses--> `Docker Deployment`  [EXTRACTED]
  render.yaml → README.md
- `FormAnalyticsServiceImpl` --implements--> `FormAnalyticsService`  [EXTRACTED]
  src/main/java/com/example/uttuCodes/formvity/service/impl/FormAnalyticsServiceImpl.java → src/main/java/com/example/uttuCodes/formvity/service/FormAnalyticsService.java
- `FormPublicationServiceImpl` --implements--> `FormPublicationService`  [EXTRACTED]
  src/main/java/com/example/uttuCodes/formvity/service/impl/FormPublicationServiceImpl.java → src/main/java/com/example/uttuCodes/formvity/service/FormPublicationService.java
- `FormServiceImpl` --implements--> `FormService`  [EXTRACTED]
  src/main/java/com/example/uttuCodes/formvity/service/impl/FormServiceImpl.java → src/main/java/com/example/uttuCodes/formvity/service/FormService.java

## Import Cycles
- None detected.

## Communities (65 total, 36 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.10
Nodes (24): TimelineBucketDto, Double, PublishFormResponseDto, SubmitFormResponseDto, FormAnalyticsSupport, QuestionAnalyticsBuilder, LocalDate, QuestionDistributionItemDto (+16 more)

### Community 1 - "Community 1"
Cohesion: 0.07
Nodes (34): ModelMapperConfig, MyUserDetailsService, UserServiceImpl, LoginDto, LoginResponse, ModelMapper, UserRepository, Bean (+26 more)

### Community 2 - "Community 2"
Cohesion: 0.12
Nodes (30): PatchMapping, PutMapping, ApiResponse, FormAnalyticsInsightsDto, FormAnalyticsOverviewDto, FormAnalyticsSummaryDto, GetMapping, List (+22 more)

### Community 3 - "Community 3"
Cohesion: 0.13
Nodes (17): AudienceInsightsDto, CompletionInsightsDto, DimensionInsightDto, Function, PublicationAccumulator, SubmissionInsightsAnalyzer, PublicationVersionInsightDto, Set (+9 more)

### Community 4 - "Community 4"
Cohesion: 0.10
Nodes (24): FormOutputDto, PublishStatusResponse, FormEntity, FormStatus, FormServiceImpl, PreUpdate, FormRepository, PrePersist (+16 more)

### Community 5 - "Community 5"
Cohesion: 0.12
Nodes (21): AccessDeniedException, BadCredentialsException, ConstraintViolationException, DataIntegrityViolationException, ErrorResponse, Exception, GlobalExceptionHandler, ExceptionHandler (+13 more)

### Community 6 - "Community 6"
Cohesion: 0.12
Nodes (19): FormvityException, WorkSpaceServiceImpl, WorkSpaceRepository, RuntimeException, String, UUID, WorkSpacesEntity, List (+11 more)

### Community 7 - "Community 7"
Cohesion: 0.09
Nodes (22): FormvityException, FormAccessService, FormPublicationServiceImpl, FormPublicationRepository, FormPublicationEntity, Optional, String, UUID (+14 more)

### Community 8 - "Community 8"
Cohesion: 0.16
Nodes (20): AnalyticsContext, FormAnalyticsServiceImpl, FormAnalyticsInsightsDto, FormAnalyticsOverviewDto, FormAnalyticsSummaryDto, FormEntity, Integer, List (+12 more)

### Community 9 - "Community 9"
Cohesion: 0.13
Nodes (16): FormPublicationService, FormPublicationEntity, Map, Object, PublishFormResponseDto, PublishStatusResponse, String, UUID (+8 more)

### Community 10 - "Community 10"
Cohesion: 0.21
Nodes (15): ApiResponse, DeleteMapping, GetMapping, List, PostMapping, ResponseEntity, String, UUID (+7 more)

### Community 11 - "Community 11"
Cohesion: 0.15
Nodes (14): AuthenticationEntryPoint, PasswordEncoderConfig, SecurityConfig, CorsConfigurationSource, HttpSecurity, PasswordEncoder, JwtAuthenticationEntryPoint, SecurityFilterChain (+6 more)

### Community 12 - "Community 12"
Cohesion: 0.20
Nodes (10): Claims, FilterChain, OncePerRequestFilter, JwtAuthFilter, JwtService, HttpServletRequest, HttpServletResponse, Override (+2 more)

### Community 13 - "Community 13"
Cohesion: 0.25
Nodes (10): Pageable, SubmissionRepository, List, LocalDateTime, Object, Optional, Page, Query (+2 more)

### Community 14 - "Community 14"
Cohesion: 0.23
Nodes (10): FormAnalyticsService, FormAnalyticsInsightsDto, FormAnalyticsOverviewDto, FormAnalyticsSummaryDto, List, Page, QuestionAnalyticsDto, SubmissionListItemDto (+2 more)

### Community 15 - "Community 15"
Cohesion: 0.24
Nodes (9): WorkSpaceService, List, UUID, WorkspaceCardDto, WorkSpaceCreateRequest, WorkspaceDashboardDto, WorkSpaceMemberInputDto, WorkSpaceOutputDto (+1 more)

### Community 16 - "Community 16"
Cohesion: 0.17
Nodes (11): SubmissionServiceImpl, SubmissionService, FormSubmissionInputDto, Override, String, SubmitFormResponseDto, Transactional, FormSubmissionInputDto (+3 more)

### Community 17 - "Community 17"
Cohesion: 0.19
Nodes (8): ApiResponse, BaseResponse, ErrorResponse, HttpStatus, String, HttpStatus, String, T

### Community 18 - "Community 18"
Cohesion: 0.25
Nodes (9): FormService, FormEntity, FormInputDto, FormOutputDto, FormPatchDto, List, Object, Optional (+1 more)

### Community 19 - "Community 19"
Cohesion: 0.21
Nodes (7): DatabaseConfig, DatabaseUrlParser, ConnectionDetails, DataSource, Primary, Bean, String

### Community 20 - "Community 20"
Cohesion: 0.20
Nodes (11): SecurityConfig, Formvity REST API, CORS Configuration, Docker Deployment, JWT Authentication, Maven Build Tool, PostgreSQL Database, Spring Boot 4 (+3 more)

### Community 21 - "Community 21"
Cohesion: 0.24
Nodes (10): FormPublicationServiceImpl, FormRoles Enum, FormServiceImpl, RBAC Authorization Model, WorkspaceAccessService, WorkspaceController, WorkspaceFormsController, WorkspaceMemberEntity (+2 more)

### Community 22 - "Community 22"
Cohesion: 0.31
Nodes (7): ApiResponse, GetMapping, List, ResponseEntity, UserDto, UserService, UserController

### Community 23 - "Community 23"
Cohesion: 0.33
Nodes (5): UserService, CurrentUser, List, UserDto, UserInputDto

### Community 24 - "Community 24"
Cohesion: 0.50
Nodes (4): WorkspaceMemberRepository, List, UUID, WorkspaceMemberEntity

### Community 25 - "Community 25"
Cohesion: 0.47
Nodes (4): GetMapping, Map, String, HealthController

## Knowledge Gaps
- **85 isolated node(s):** `String`, `ConnectionDetails`, `String`, `Override`, `CurrentUser` (+80 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **36 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FormvityException` connect `Community 7` to `Community 1`, `Community 4`, `Community 5`, `Community 6`, `Community 8`, `Community 16`?**
  _High betweenness centrality (0.125) - this node is a cross-community bridge._
- **Why does `FormStatus` connect `Community 4` to `Community 6`, `Community 7`?**
  _High betweenness centrality (0.065) - this node is a cross-community bridge._
- **Why does `WorkSpaceService` connect `Community 15` to `Community 10`, `Community 6`?**
  _High betweenness centrality (0.045) - this node is a cross-community bridge._
- **What connects `String`, `ConnectionDetails`, `String` to the rest of the system?**
  _85 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.09696969696969697 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.06734006734006734 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.12329931972789115 - nodes in this community are weakly interconnected._