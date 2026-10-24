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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 3 评价闭环与统计完善的接口级回归测试。
 */
@SpringBootTest(properties = {
        "app.auth.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:sprint3_test;MODE=MySQL;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Sprint3IntegrationTest {

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
    private UserAccount teacherTwoUser;
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
        teacherTwoUser = account("teacher2", "李华", UserRole.TEACHER, teacherTwo);
        account("supervisor", "周督导", UserRole.SUPERVISOR, null);
        account("director", "孟主任", UserRole.DIRECTOR, null);
        account("admin", "系统管理员", UserRole.ADMIN, null);

        activeResource = resource(
                offeringOne,
                "程序结构推荐教案",
                ResourceType.DOCUMENT,
                true);
        inactiveResource = resource(
                offeringTwo,
                "过时数据结构课件",
                ResourceType.SLIDES,
                false);
    }

    @Test
    void courseDrilldownMatchesCourseListAndRestrictsRoles() throws Exception {
        String token = login("director", "director123");
        String listBody = mockMvc.perform(get("/api/courses")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("semesterId", semester.getId().toString())
                        .param("departmentId", softwareDepartment.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn().getResponse().getContentAsString();

        String drilldownBody = mockMvc.perform(get("/api/course-drilldown")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("semesterId", semester.getId().toString())
                        .param("departmentId", softwareDepartment.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentCount").value(1))
                .andExpect(jsonPath("$.offeringCount").value(2))
                .andExpect(jsonPath("$.studentCount").value(112))
                .andExpect(jsonPath("$.departments[0].departmentCode").value("SOFTWARE"))
                .andExpect(jsonPath("$.departments[0].courses.length()").value(2))
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(drilldownBody).get("departments").get(0).get("courses"))
                .isEqualTo(objectMapper.readTree(listBody));

        mockMvc.perform(get("/api/course-drilldown")
                        .header(HttpHeaders.AUTHORIZATION,
                                bearer(login("teacher", "teacher123"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/supervision/coverage")
                        .header(HttpHeaders.AUTHORIZATION,
                                bearer(login("teacher", "teacher123"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherSubscriptionsDriveCourseChangeNotificationsAndCanBeUnsubscribed()
            throws Exception {
        String teacherToken = login("teacher", "teacher123");
        mockMvc.perform(post("/api/notifications/subscriptions/{courseId}", courseOne.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscribed").value(true));

        mockMvc.perform(post("/api/notifications/subscriptions/{courseId}", courseOne.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                bearer(login("teacher2", "teacher123"))))
                .andExpect(status().isForbidden());

        importOfferingChange("2025-2026-2", "CS101", "张明", "软件2301", 59, "OPEN");

        mockMvc.perform(get("/api/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1))
                .andExpect(jsonPath("$.notifications[0].courseCode").value("CS101"))
                .andExpect(jsonPath("$.notifications[0].read").value(false))
                .andExpect(jsonPath("$.courses[0].subscribed").value(true));

        importOfferingChange("2025-2026-2", "CS101", "张明", "软件2301", 59, "OPEN");
        mockMvc.perform(get("/api/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications.length()").value(1));

        mockMvc.perform(patch("/api/notifications/read")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk());
        assertThat(notificationRepository.countByRecipientIdAndReadAtIsNull(
                teacherUser.getId())).isZero();

        mockMvc.perform(delete("/api/notifications/subscriptions/{courseId}", courseOne.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscribed").value(false));
        importOfferingChange("2025-2026-2", "CS101", "张明", "软件2301", 60, "OPEN");

        mockMvc.perform(get("/api/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications.length()").value(1));
        assertThat(teacherTwoUser.getId()).isNotNull();
    }

    @Test
    void supervisionPlansTrackProgressAndOneOfferingHasOneSupervisionRecord()
            throws Exception {
        String token = login("supervisor", "supervisor123");
        LocalDate today = LocalDate.now();
        long overduePlanId = createPlan(
                token, offeringOne, today.minusDays(3), today.minusDays(1), "逾期计划");
        long activePlanId = createPlan(
                token, offeringOne, today, today.plusDays(2), "本周计划");

        mockMvc.perform(get("/api/supervision/plans")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("semesterId", semester.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.total").value(2))
                .andExpect(jsonPath("$.summary.overdue").value(1));

        completePlan(token, overduePlanId);
        mockMvc.perform(patch("/api/supervision/plans/{planId}/status", activePlanId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COMPLETED"}
                                """))
                .andExpect(status().isOk());

        assertThat(supervisionRepository.countByOfferingId(offeringOne.getId())).isEqualTo(1);
        mockMvc.perform(get("/api/supervision/plans")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.completed").value(2))
                .andExpect(jsonPath("$.summary.overdue").value(0))
                .andExpect(jsonPath("$.summary.progressRate").value(100.0));
    }

    @Test
    void supervisionRecordUniqueConstraintRejectsDuplicates() {
        supervisionRepository.saveAndFlush(SupervisionRecord.builder()
                .offering(offeringThree)
                .supervisor("测试督导")
                .supervisedAt(LocalDate.now())
                .build());

        assertThatThrownBy(() -> supervisionRepository.saveAndFlush(
                SupervisionRecord.builder()
                        .offering(offeringThree)
                        .supervisor("重复督导")
                        .supervisedAt(LocalDate.now())
                        .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void teacherCanUnpublishAndRestoreOwnedResourceWhileSearchHidesIt()
            throws Exception {
        String teacherToken = login("teacher", "teacher123");
        mockMvc.perform(get("/api/resources/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .param("keyword", "程序结构"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(patch("/api/resources/{resourceId}/availability", activeResource.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"active":false,"reason":"课程内容已更新"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.unavailableAt").isNotEmpty());

        mockMvc.perform(get("/api/resources/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .param("keyword", "程序结构"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/courses/{offeringId}", offeringOne.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resources[0].active").value(false));

        mockMvc.perform(patch("/api/resources/{resourceId}/availability", activeResource.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                bearer(login("teacher2", "teacher123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"active":true}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/resources/{resourceId}/availability", activeResource.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"active":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/resources/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken))
                        .param("keyword", "程序结构"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void supervisorCanReviewAndRecommendQualityResources() throws Exception {
        String supervisorToken = login("supervisor", "supervisor123");
        mockMvc.perform(post("/api/resources/{resourceId}/review", activeResource.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(supervisorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "level":"EXCELLENT",
                                  "comment":"结构清晰，适合作为课程示范资源",
                                  "recommended":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value("EXCELLENT"))
                .andExpect(jsonPath("$.levelLabel").value("优质"))
                .andExpect(jsonPath("$.recommended").value(true));

        mockMvc.perform(get("/api/resources/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer(supervisorToken))
                        .param("keyword", "程序结构"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].qualityLevel")
                        .value(ResourceQualityLevel.EXCELLENT.name()))
                .andExpect(jsonPath("$[0].recommended").value(true));

        mockMvc.perform(post("/api/resources/{resourceId}/review", activeResource.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                bearer(login("teacher", "teacher123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"level":"QUALIFIED","comment":"越权评审","recommended":false}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void resourceCoverageCountsOnlyPublishedResourcesAndLocatesLowCoverageCourses()
            throws Exception {
        String token = login("director", "director123");
        mockMvc.perform(get("/api/resource-coverage")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("semesterId", semester.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.offeringCount").value(3))
                .andExpect(jsonPath("$.summary.coveredOfferingCount").value(1))
                .andExpect(jsonPath("$.summary.uncoveredOfferingCount").value(2))
                .andExpect(jsonPath("$.summary.resourceCount").value(1))
                .andExpect(jsonPath("$.courses[0].covered").value(false))
                .andExpect(jsonPath("$.courses[2].courseCode").value("CS101"))
                .andExpect(jsonPath("$.courses[2].resourceCount").value(1));

        assertThat(inactiveResource.isActive()).isFalse();
    }

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
                                {
                                  "offeringId":%d,
                                  "plannedDate":"%s",
                                  "dueDate":"%s",
                                  "note":"%s"
                                }
                                """.formatted(
                                offering.getId(),
                                plannedDate,
                                dueDate,
                                note)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SupervisionPlanStatus.PLANNED.name()))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void completePlan(String token, long planId) throws Exception {
        mockMvc.perform(patch("/api/supervision/plans/{planId}/status", planId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COMPLETED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());
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
                .formatted(
                        courseCode,
                        teacherName,
                        semesterCode,
                        className,
                        studentCount,
                        status);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "course-change.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/import/courses")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION,
                                bearer(login("admin", "admin123"))))
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
