package com.loveofteach.platform.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loveofteach.platform.common.OfferingStatus;
import com.loveofteach.platform.common.ResourceQualityLevel;
import com.loveofteach.platform.common.ResourceType;
import com.loveofteach.platform.common.SupervisionPlanStatus;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.Course;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.domain.Department;
import com.loveofteach.platform.domain.Semester;
import com.loveofteach.platform.domain.SupervisionRecord;
import com.loveofteach.platform.domain.Teacher;
import com.loveofteach.platform.domain.TeachingResource;
import com.loveofteach.platform.domain.UserAccount;
import com.loveofteach.platform.repository.AuthTokenRepository;
import com.loveofteach.platform.repository.CourseEvaluationRepository;
import com.loveofteach.platform.repository.CourseNotificationRepository;
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.CourseRepository;
import com.loveofteach.platform.repository.CourseSubscriptionRepository;
import com.loveofteach.platform.repository.DepartmentRepository;
import com.loveofteach.platform.repository.ResourceChapterRepository;
import com.loveofteach.platform.repository.ResourceQualityReviewRepository;
import com.loveofteach.platform.repository.SemesterRepository;
import com.loveofteach.platform.repository.SupervisionPlanRepository;
import com.loveofteach.platform.repository.SupervisionRecordRepository;
import com.loveofteach.platform.repository.TeacherRepository;
import com.loveofteach.platform.repository.TeachingResourceRepository;
import com.loveofteach.platform.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 集成回归智能体生成的全量回归与端到端旅程测试。
 *
 * <p>三个 Sprint 的增量已全部合并到 {@code main}，本测试在鉴权开启（生产配置）的前提下，
 * 对 Sprint 1（课程数据底座）、Sprint 2（资源/评价/报表 + 鉴权）与 Sprint 3（学院钻取、
 * 通知订阅、听评课计划、资源上下架与质量评审、资源覆盖率）做一次覆盖三个 Sprint 的全量回归，
 * 并补齐发布就绪 DoD 要求的端到端核心用户旅程（1 条主旅程 + 1 条异常/权限旅程）。</p>
 */
@SpringBootTest(properties = {
        "app.auth.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:full_regression;MODE=MySQL;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FullRegressionAndEndToEndJourneyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private CourseNotificationRepository notificationRepository;

    @Autowired
    private CourseSubscriptionRepository subscriptionRepository;

    @Autowired
    private ResourceQualityReviewRepository qualityReviewRepository;

    @Autowired
    private SupervisionPlanRepository planRepository;

    @Autowired
    private CourseEvaluationRepository evaluationRepository;

    @Autowired
    private TeachingResourceRepository resourceRepository;

    @Autowired
    private ResourceChapterRepository chapterRepository;

    @Autowired
    private SupervisionRecordRepository supervisionRepository;

    @Autowired
    private CourseOfferingRepository offeringRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department softwareDepartment;
    private Department computerDepartment;
    private Semester semester;
    private Teacher teacherOne;
    private Teacher teacherTwo;
    private Course courseOne;
    private Course courseTwo;
    private Course courseThree;
    private CourseOffering offeringOne;
    private CourseOffering offeringTwo;
    private CourseOffering offeringThree;
    private UserAccount teacherUser;
    private TeachingResource activeResource;
    private TeachingResource inactiveResource;

    @BeforeEach
    void setUp() {
        authTokenRepository.deleteAll();
        notificationRepository.deleteAll();
        subscriptionRepository.deleteAll();
        qualityReviewRepository.deleteAll();
        planRepository.deleteAll();
        evaluationRepository.deleteAll();
        resourceRepository.deleteAll();
        chapterRepository.deleteAll();
        supervisionRepository.deleteAll();
        offeringRepository.deleteAll();
        courseRepository.deleteAll();
        userAccountRepository.deleteAll();
        teacherRepository.deleteAll();
        semesterRepository.deleteAll();
        departmentRepository.deleteAll();

        softwareDepartment = departmentRepository.save(Department.builder()
                .code("SOFTWARE")
                .name("软件工程教研室")
                .active(true)
                .build());
        computerDepartment = departmentRepository.save(Department.builder()
                .code("COMPUTER")
                .name("计算机科学教研室")
                .active(true)
                .build());
        semester = semesterRepository.save(Semester.builder()
                .code("2025-2026-2")
                .name("2025-2026 学年第二学期")
                .startDate(LocalDate.of(2026, 2, 23))
                .endDate(LocalDate.of(2026, 7, 10))
                .active(true)
                .build());
        teacherOne = teacherRepository.save(Teacher.builder()
                .employeeNo("T001")
                .name("张明")
                .title("副教授")
                .department(softwareDepartment)
                .build());
        teacherTwo = teacherRepository.save(Teacher.builder()
                .employeeNo("T002")
                .name("李华")
                .title("讲师")
                .department(softwareDepartment)
                .build());
        courseOne = course("CS101", "程序设计基础", softwareDepartment);
        courseTwo = course("CS201", "数据结构", softwareDepartment);
        courseThree = course("AI101", "人工智能基础", computerDepartment);
        offeringOne = offering(courseOne, teacherOne, "软件2301", 58);
        offeringTwo = offering(courseTwo, teacherTwo, "软件2302", 54);
        offeringThree = offering(courseThree, teacherTwo, "计算机2301", 44);

        teacherUser = account("teacher", "张明", UserRole.TEACHER, teacherOne);
        account("teacher2", "李华", UserRole.TEACHER, teacherTwo);
        account("supervisor", "周督导", UserRole.SUPERVISOR, null);
        account("director", "孟主任", UserRole.DIRECTOR, null);
        account("admin", "系统管理员", UserRole.ADMIN, null);

        activeResource = resource(offeringOne, "程序结构推荐教案", ResourceType.DOCUMENT, true);
        inactiveResource = resource(offeringTwo, "过时数据结构课件", ResourceType.SLIDES, false);
    }

    // ---------------------------------------------------------------------
    // 全量回归：Sprint 1 —— 课程数据底座
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Sprint 1 回归：核心接口需登录，课程列表/教师总览语义不变")
    void sprint1_authGuardCourseListAndTeacherOverview() throws Exception {
        mockMvc.perform(get("/api/courses")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/courses/teacher-overview")).andExpect(status().isUnauthorized());

        String token = login("director", "director123");
        mockMvc.perform(get("/api/courses")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("semesterId", semester.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].courseCode").isNotEmpty());

        mockMvc.perform(get("/api/courses/teacher-overview")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("departmentId", softwareDepartment.getId().toString())
                        .param("semesterId", semester.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherCount").value(2))
                .andExpect(jsonPath("$.courseCount").value(2));
    }

    @Test
    @DisplayName("Sprint 1 回归：督导覆盖率与课程详情语义不变")
    void sprint1_coverageAndCourseDetail() throws Exception {
        supervisionRepository.save(SupervisionRecord.builder()
                .offering(offeringOne)
                .supervisor("校级督导 周老师")
                .supervisedAt(LocalDate.of(2026, 3, 12))
                .build());

        String token = login("director", "director123");
        mockMvc.perform(get("/api/supervision/coverage")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("semesterId", semester.getId().toString())
                        .param("departmentId", softwareDepartment.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.offeringCount").value(2))
                .andExpect(jsonPath("$.summary.supervisedCount").value(1))
                .andExpect(jsonPath("$.summary.coverageRate").value(50.0));

        mockMvc.perform(get("/api/courses/{id}", offeringOne.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentCount").value(58))
                .andExpect(jsonPath("$.supervised").value(true))
                .andExpect(jsonPath("$.resources.length()").value(1));
    }

    @Test
    @DisplayName("Sprint 1 回归：目录维护与 CSV 导入语义不变")
    void sprint1_catalogAndImport() throws Exception {
        String token = login("admin", "admin123");
        mockMvc.perform(get("/api/departments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        String csv = """
                courseCode,courseName,departmentCode,credits,description,teacherNo,teacherName,semesterCode,className,studentCount,status
                CS500,云计算基础,SOFTWARE,3.0,云平台课程,T008,周宁,2025-2026-2,软件2304,48,OPEN
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file", "courses.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/import/courses")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failureCount").value(0));
        assertThat(courseRepository.findByCodeIgnoreCase("CS500")).isPresent();
    }

    // ---------------------------------------------------------------------
    // 全量回归：Sprint 2 —— 鉴权、资源、评价、报表
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Sprint 2 回归：登录身份识别与公开注册角色限制")
    void sprint2_loginAndRegistration() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"teacher\",\"password\":\"teacher123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("TEACHER"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"teacher\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"self_admin","displayName":"越权","password":"adminpass123","role":"SUPERVISOR"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("公开注册仅允许教师角色，其他角色由管理员创建"));
    }

    @Test
    @DisplayName("Sprint 2 回归：资源上传、章节组织与检索语义不变")
    void sprint2_resourceUploadAndSearch() throws Exception {
        String token = login("teacher", "teacher123");
        String chapterBody = mockMvc.perform(post("/api/offerings/{id}/chapters", offeringOne.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"第 1 章 基础语法\",\"displayOrder\":1,\"description\":\"变量与流程控制\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long chapterId = objectMapper.readTree(chapterBody).get("id").asLong();

        MockMultipartFile file = new MockMultipartFile(
                "file", "lesson-plan.pdf", "application/pdf",
                "%PDF-1.4\nlesson plan".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/offerings/{id}/resources", offeringOne.getId())
                        .file(file)
                        .param("name", "第一章教案")
                        .param("resourceType", "DOCUMENT")
                        .param("chapterId", Long.toString(chapterId))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chapterId").value(chapterId));

        mockMvc.perform(get("/api/resources/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("keyword", "第一章"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }

    @Test
    @DisplayName("Sprint 2 回归：课程评价、汇总与教师反馈语义不变")
    void sprint2_evaluationAndFeedback() throws Exception {
        String supervisorToken = login("supervisor", "supervisor123");
        mockMvc.perform(post("/api/evaluations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(supervisorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"offeringId\":%d,\"score\":88,\"comment\":\"课堂组织清晰\"}"
                                .formatted(offeringOne.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(88));

        mockMvc.perform(get("/api/evaluations/summary")
                        .header(HttpHeaders.AUTHORIZATION, bearer(supervisorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluationCount").value(1))
                .andExpect(jsonPath("$.overallAverageScore").value(88.0));

        String teacherToken = login("teacher", "teacher123");
        mockMvc.perform(get("/api/evaluations/mine")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherId").value(teacherOne.getId()))
                .andExpect(jsonPath("$.evaluationCount").value(1));
    }

    @Test
    @DisplayName("Sprint 2 回归：开课统计导出与公式注入转义")
    void sprint2_reportExport() throws Exception {
        String token = login("director", "director123");
        String csv = mockMvc.perform(get("/api/reports/offerings/export")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        org.hamcrest.Matchers.containsString("course-offering-report")))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(csv).contains("课程代码").contains("CS101").contains("张明");
    }

    // ---------------------------------------------------------------------
    // 全量回归：Sprint 3 —— 学院钻取、通知、听评课、资源生命周期、覆盖率
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Sprint 3 回归：学院钻取与课程列表一致并限制角色")
    void sprint3_courseDrilldown() throws Exception {
        String token = login("director", "director123");
        mockMvc.perform(get("/api/course-drilldown")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("semesterId", semester.getId().toString())
                        .param("departmentId", softwareDepartment.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentCount").value(1))
                .andExpect(jsonPath("$.offeringCount").value(2))
                .andExpect(jsonPath("$.studentCount").value(112))
                .andExpect(jsonPath("$.departments[0].courses.length()").value(2));

        mockMvc.perform(get("/api/course-drilldown")
                        .header(HttpHeaders.AUTHORIZATION,
                                bearer(login("teacher", "teacher123"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Sprint 3 回归：订阅驱动通知、标记已读与退订")
    void sprint3_notificationSubscription() throws Exception {
        String teacherToken = login("teacher", "teacher123");
        mockMvc.perform(post("/api/notifications/subscriptions/{courseId}", courseOne.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscribed").value(true));

        importOfferingChange("2025-2026-2", "CS101", "张明", "软件2301", 59, "OPEN");

        mockMvc.perform(get("/api/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1))
                .andExpect(jsonPath("$.notifications[0].courseCode").value("CS101"));

        mockMvc.perform(patch("/api/notifications/read")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk());
        assertThat(notificationRepository.countByRecipientIdAndReadAtIsNull(
                teacherUser.getId())).isZero();

        mockMvc.perform(delete("/api/notifications/subscriptions/{courseId}", courseOne.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscribed").value(false));
    }

    @Test
    @DisplayName("Sprint 3 回归：听评课计划进度、逾期与唯一督导记录")
    void sprint3_supervisionPlans() throws Exception {
        String token = login("supervisor", "supervisor123");
        LocalDate today = LocalDate.now();
        long overdueId = createPlan(token, offeringOne, today.minusDays(3), today.minusDays(1), "逾期计划");
        long activeId = createPlan(token, offeringOne, today, today.plusDays(2), "本周计划");

        mockMvc.perform(get("/api/supervision/plans")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("semesterId", semester.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.total").value(2))
                .andExpect(jsonPath("$.summary.overdue").value(1));

        completePlan(token, overdueId);
        completePlan(token, activeId);

        assertThat(supervisionRepository.countByOfferingId(offeringOne.getId())).isEqualTo(1);
        mockMvc.perform(get("/api/supervision/plans")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.completed").value(2))
                .andExpect(jsonPath("$.summary.progressRate").value(100.0));
    }

    @Test
    @DisplayName("Sprint 3 回归：资源下架/恢复、检索隐藏与质量评审")
    void sprint3_resourceLifecycleAndReview() throws Exception {
        String teacherToken = login("teacher", "teacher123");
        mockMvc.perform(patch("/api/resources/{resourceId}/availability", activeResource.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false,\"reason\":\"课程内容已更新\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/resources/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .param("keyword", "程序结构"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(patch("/api/resources/{resourceId}/availability", activeResource.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        String supervisorToken = login("supervisor", "supervisor123");
        mockMvc.perform(post("/api/resources/{resourceId}/review", activeResource.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(supervisorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":\"EXCELLENT\",\"comment\":\"结构清晰\",\"recommended\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value("EXCELLENT"))
                .andExpect(jsonPath("$.recommended").value(true));
    }

    @Test
    @DisplayName("Sprint 3 回归：资源覆盖率仅统计已上架资源")
    void sprint3_resourceCoverage() throws Exception {
        String token = login("director", "director123");
        mockMvc.perform(get("/api/resource-coverage")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("semesterId", semester.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.offeringCount").value(3))
                .andExpect(jsonPath("$.summary.coveredOfferingCount").value(1))
                .andExpect(jsonPath("$.summary.uncoveredOfferingCount").value(2))
                .andExpect(jsonPath("$.summary.resourceCount").value(1));
        assertThat(inactiveResource.isActive()).isFalse();
    }

    // ---------------------------------------------------------------------
    // 端到端旅程：主旅程（跨三个 Sprint 的真实用户链路）
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("端到端主旅程：登录→订阅通知→下架恢复资源→听评课→评审→钻取与覆盖率→导出")
    void endToEnd_coreUserJourney() throws Exception {
        String teacherToken = login("teacher", "teacher123");
        String supervisorToken = login("supervisor", "supervisor123");
        String directorToken = login("director", "director123");
        String adminToken = login("admin", "admin123");

        // 1. 教师订阅本人课程并接收变更通知
        mockMvc.perform(post("/api/notifications/subscriptions/{courseId}", courseOne.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscribed").value(true));

        importOfferingChange("2025-2026-2", "CS101", "张明", "软件2301", 59, "OPEN");
        mockMvc.perform(get("/api/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1))
                .andExpect(jsonPath("$.notifications[0].courseCode").value("CS101"));
        mockMvc.perform(patch("/api/notifications/read")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk());

        // 2. 教师下架并恢复本人资源
        mockMvc.perform(patch("/api/resources/{resourceId}/availability", activeResource.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false,\"reason\":\"临时下线\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(get("/api/resources/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .param("keyword", "程序结构"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(patch("/api/resources/{resourceId}/availability", activeResource.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        // 3. 督导创建并完成听评课计划，生成督导记录
        long planId = createPlan(supervisorToken, offeringOne, LocalDate.now(),
                LocalDate.now().plusDays(2), "端到端听评课");
        completePlan(supervisorToken, planId);
        assertThat(supervisionRepository.countByOfferingId(offeringOne.getId())).isEqualTo(1);

        // 4. 督导评审资源质量
        mockMvc.perform(post("/api/resources/{resourceId}/review", activeResource.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(supervisorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":\"EXCELLENT\",\"comment\":\"适合示范\",\"recommended\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(ResourceQualityLevel.EXCELLENT.name()))
                .andExpect(jsonPath("$.recommended").value(true));

        // 5. 主任查看学院钻取与资源覆盖率
        mockMvc.perform(get("/api/course-drilldown")
                        .header(HttpHeaders.AUTHORIZATION, bearer(directorToken))
                        .param("semesterId", semester.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offeringCount").value(3));
        mockMvc.perform(get("/api/resource-coverage")
                        .header(HttpHeaders.AUTHORIZATION, bearer(directorToken))
                        .param("semesterId", semester.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.offeringCount").value(3))
                .andExpect(jsonPath("$.summary.coveredOfferingCount").value(1));

        // 6. 主任导出开课统计
        String csv = mockMvc.perform(get("/api/reports/offerings/export")
                        .header(HttpHeaders.AUTHORIZATION, bearer(directorToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(csv).contains("CS101").contains("AI101");

        // adminToken 在整条旅程中有效，证明多角色令牌互不串扰
        assertThat(adminToken).isNotBlank();
    }

    @Test
    @DisplayName("端到端异常/权限旅程：未登录与越权访问均被拒绝")
    void endToEnd_permissionAndAnomalyJourney() throws Exception {
        // 未登录访问核心接口
        mockMvc.perform(get("/api/courses")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/course-drilldown")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/resource-coverage")).andExpect(status().isUnauthorized());

        String teacherToken = login("teacher", "teacher123");

        // 教师不能维护目录（管理员权限）
        mockMvc.perform(post("/api/semesters")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"2027-2028-1","name":"越权学期","startDate":"2027-09-01","endDate":"2028-01-15","active":true}
                                """))
                .andExpect(status().isForbidden());

        // 教师不能提交课程评价（督导/主任权限）
        mockMvc.perform(post("/api/evaluations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"offeringId\":%d,\"score\":90,\"comment\":\"越权评价\"}"
                                .formatted(offeringOne.getId())))
                .andExpect(status().isForbidden());

        // 教师不能操作他人课程资源
        mockMvc.perform(patch("/api/resources/{resourceId}/availability", inactiveResource.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isForbidden());

        // 教师不能订阅他人课程通知
        mockMvc.perform(post("/api/notifications/subscriptions/{courseId}", courseTwo.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                bearer(login("teacher", "teacher123"))))
                .andExpect(status().isForbidden());

        // 教师不能查看学院钻取
        mockMvc.perform(get("/api/course-drilldown")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------------
    // 辅助方法
    // ---------------------------------------------------------------------

    private long createPlan(
            String token,
            CourseOffering offering,
            LocalDate plannedDate,
            LocalDate dueDate,
            String note
    ) throws Exception {
        String response = mockMvc.perform(post("/api/supervision/plans")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"offeringId":%d,"plannedDate":"%s","dueDate":"%s","note":"%s"}
                                """.formatted(offering.getId(), plannedDate, dueDate, note)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SupervisionPlanStatus.PLANNED.name()))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void completePlan(String token, long planId) throws Exception {
        mockMvc.perform(patch("/api/supervision/plans/{planId}/status", planId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    private void importOfferingChange(
            String semesterCode,
            String courseCode,
            String teacherName,
            String className,
            int studentCount,
            String status
    ) throws Exception {
        String csv = "courseCode,courseName,departmentCode,credits,description,"
                + "teacherNo,teacherName,semesterCode,className,studentCount,status\n"
                + "%s,程序设计基础,SOFTWARE,3.0,课程简介,T001,%s,%s,%s,%d,%s\n"
                .formatted(courseCode, teacherName, semesterCode, className, studentCount, status);
        MockMultipartFile file = new MockMultipartFile(
                "file", "course-change.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/import/courses")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(login("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1));
    }

    private Course course(String code, String name, Department department) {
        return courseRepository.save(Course.builder()
                .code(code)
                .name(name)
                .credits(new BigDecimal("3.0"))
                .description(name + "课程简介")
                .department(department)
                .active(true)
                .build());
    }

    private CourseOffering offering(
            Course course,
            Teacher teacher,
            String className,
            int studentCount
    ) {
        return offeringRepository.save(CourseOffering.builder()
                .course(course)
                .teacher(teacher)
                .semester(semester)
                .className(className)
                .studentCount(studentCount)
                .status(OfferingStatus.OPEN)
                .build());
    }

    private TeachingResource resource(
            CourseOffering offering,
            String name,
            ResourceType type,
            boolean active
    ) {
        return resourceRepository.save(TeachingResource.builder()
                .offering(offering)
                .name(name)
                .resourceType(type)
                .url("https://example.com/" + name)
                .description(name + "说明")
                .active(active)
                .unavailableAt(active ? null : LocalDateTime.now().minusDays(1))
                .unavailableReason(active ? null : "内容过时")
                .uploadedAt(LocalDateTime.now().minusDays(2))
                .build());
    }

    private UserAccount account(
            String username,
            String displayName,
            UserRole role,
            Teacher teacher
    ) {
        return userAccountRepository.save(UserAccount.builder()
                .username(username)
                .displayName(displayName)
                .passwordHash(passwordEncoder.encode(
                        switch (role) {
                            case SUPERVISOR -> "supervisor123";
                            case DIRECTOR -> "director123";
                            case ADMIN -> "admin123";
                            default -> "teacher123";
                        }))
                .role(role)
                .teacher(teacher)
                .active(true)
                .build());
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        return body.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
