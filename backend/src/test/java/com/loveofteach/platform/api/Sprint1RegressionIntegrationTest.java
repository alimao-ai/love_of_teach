package com.loveofteach.platform.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loveofteach.platform.common.OfferingStatus;
import com.loveofteach.platform.common.ResourceType;
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
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.CourseRepository;
import com.loveofteach.platform.repository.DepartmentRepository;
import com.loveofteach.platform.repository.SemesterRepository;
import com.loveofteach.platform.repository.SupervisionRecordRepository;
import com.loveofteach.platform.repository.TeacherRepository;
import com.loveofteach.platform.repository.TeachingResourceRepository;
import com.loveofteach.platform.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 1 既有功能的回归测试。
 *
 * <p>Sprint 2 引入了基于令牌的接口鉴权（{@code app.auth.enabled=true} 为生产配置），
 * 本测试在鉴权开启的前提下，验证 Sprint 1 交付的课程查询、教师总览、督导覆盖率、
 * 课程详情、目录维护与 CSV 导入等既有接口仍按原有语义正常工作，确保 Sprint 2 的
 * 鉴权与权限改造没有破坏 Sprint 1 的既有功能。</p>
 */
@SpringBootTest(properties = {
        "app.auth.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:sprint1_regression;MODE=MySQL;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Sprint1RegressionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseOfferingRepository offeringRepository;

    @Autowired
    private TeachingResourceRepository resourceRepository;

    @Autowired
    private SupervisionRecordRepository supervisionRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    private Department software;
    private Semester spring;
    private Teacher teacherOne;
    private Teacher teacherTwo;
    private CourseOffering offeringOne;
    private CourseOffering offeringTwo;

    @BeforeEach
    void setUp() {
        authTokenRepository.deleteAll();
        resourceRepository.deleteAll();
        supervisionRepository.deleteAll();
        offeringRepository.deleteAll();
        courseRepository.deleteAll();
        userAccountRepository.deleteAll();
        teacherRepository.deleteAll();
        semesterRepository.deleteAll();
        departmentRepository.deleteAll();

        software = departmentRepository.save(Department.builder()
                .code("SOFTWARE")
                .name("软件工程教研室")
                .active(true)
                .build());
        spring = semesterRepository.save(Semester.builder()
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
                .department(software)
                .build());
        teacherTwo = teacherRepository.save(Teacher.builder()
                .employeeNo("T002")
                .name("李华")
                .title("讲师")
                .department(software)
                .build());

        Course programming = courseRepository.save(Course.builder()
                .code("CS101")
                .name("程序设计基础")
                .credits(new BigDecimal("3.0"))
                .description("程序设计入门课程")
                .department(software)
                .active(true)
                .build());
        Course dataStructure = courseRepository.save(Course.builder()
                .code("CS201")
                .name("数据结构")
                .credits(new BigDecimal("4.0"))
                .description("数据结构核心课程")
                .department(software)
                .active(true)
                .build());

        offeringOne = offeringRepository.save(CourseOffering.builder()
                .course(programming)
                .teacher(teacherOne)
                .semester(spring)
                .className("软件2301")
                .studentCount(58)
                .status(OfferingStatus.OPEN)
                .build());
        offeringTwo = offeringRepository.save(CourseOffering.builder()
                .course(dataStructure)
                .teacher(teacherTwo)
                .semester(spring)
                .className("软件2302")
                .studentCount(54)
                .status(OfferingStatus.OPEN)
                .build());

        resourceRepository.save(TeachingResource.builder()
                .offering(offeringOne)
                .name("课程导论")
                .resourceType(ResourceType.DOCUMENT)
                .url("https://example.com/cs101-intro.pdf")
                .uploadedAt(LocalDateTime.now().minusDays(1))
                .build());
        supervisionRepository.save(SupervisionRecord.builder()
                .offering(offeringOne)
                .supervisor("校级督导 周老师")
                .supervisedAt(LocalDate.of(2026, 3, 12))
                .build());

        userAccountRepository.save(UserAccount.builder()
                .username("director")
                .displayName("孟主任")
                .passwordHash(passwordEncoder.encode("director123"))
                .role(UserRole.DIRECTOR)
                .active(true)
                .build());
        userAccountRepository.save(UserAccount.builder()
                .username("admin")
                .displayName("系统管理员")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .active(true)
                .build());
    }

    @Test
    void sprint1EndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/courses/teacher-overview"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/supervision/coverage"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/semesters"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/import/courses/template"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void courseListKeepsSprint1SemanticsWhenAuthenticated() throws Exception {
        String token = login("director", "director123");
        mockMvc.perform(get("/api/courses")
                        .param("semesterId", spring.getId().toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].courseCode").isNotEmpty())
                .andExpect(jsonPath("$[0].resourceCount").isNumber());

        mockMvc.perform(get("/api/courses")
                        .param("semesterId", spring.getId().toString())
                        .param("teacherId", teacherOne.getId().toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }

    @Test
    void teacherOverviewKeepsSprint1SemanticsWhenAuthenticated() throws Exception {
        String token = login("director", "director123");
        mockMvc.perform(get("/api/courses/teacher-overview")
                        .param("departmentId", software.getId().toString())
                        .param("semesterId", spring.getId().toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherCount").value(2))
                .andExpect(jsonPath("$.courseCount").value(2))
                .andExpect(jsonPath("$.teachers.length()").value(2));
    }

    @Test
    void coverageKeepsSprint1SemanticsWhenAuthenticated() throws Exception {
        String token = login("director", "director123");
        mockMvc.perform(get("/api/supervision/coverage")
                        .param("semesterId", spring.getId().toString())
                        .param("departmentId", software.getId().toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.offeringCount").value(2))
                .andExpect(jsonPath("$.summary.supervisedCount").value(1))
                .andExpect(jsonPath("$.summary.unsupervisedCount").value(1))
                .andExpect(jsonPath("$.summary.coverageRate").value(50.0));
    }

    @Test
    void courseDetailKeepsSprint1SemanticsWhenAuthenticated() throws Exception {
        String token = login("director", "director123");
        mockMvc.perform(get("/api/courses/{id}", offeringOne.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentCount").value(58))
                .andExpect(jsonPath("$.description").value("程序设计入门课程"))
                .andExpect(jsonPath("$.supervised").value(true))
                .andExpect(jsonPath("$.resources.length()").value(1));
    }

    @Test
    void catalogListAndGetKeepsSprint1SemanticsWhenAuthenticated() throws Exception {
        String token = login("director", "director123");
        mockMvc.perform(get("/api/departments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("SOFTWARE"));

        mockMvc.perform(get("/api/departments/{id}", software.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SOFTWARE"));

        mockMvc.perform(get("/api/semesters")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/teachers")
                        .param("departmentId", software.getId().toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void catalogCreateUpdateKeepsSprint1SemanticsWhenAuthenticated() throws Exception {
        // Catalog writes are administrator-only after the Sprint 2 review fix.
        String token = login("admin", "admin123");

        mockMvc.perform(post("/api/semesters")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "2027-2028-1",
                                  "name": "2027-2028 学年第一学期",
                                  "startDate": "2027-09-01",
                                  "endDate": "2028-01-15",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("2027-2028-1"));

        mockMvc.perform(put("/api/departments/{id}", software.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "SOFTWARE",
                                  "name": "软件工程教研室（更新）",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("软件工程教研室（更新）"));

        mockMvc.perform(patch("/api/semesters/{id}/status", spring.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void importKeepsSprint1SemanticsWhenAuthenticated() throws Exception {
        // Course import is administrator-only after the Sprint 2 review fix.
        String token = login("admin", "admin123");
        String csv = """
                courseCode,courseName,departmentCode,credits,description,teacherNo,teacherName,semesterCode,className,studentCount,status
                CS500,云计算基础,SOFTWARE,3.0,云平台课程,T008,周宁,2025-2026-2,软件2304,48,OPEN
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "courses.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/import/courses")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(1))
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failureCount").value(0));

        assertThat(courseRepository.findByCodeIgnoreCase("CS500")).isPresent();
    }

    @Test
    void importTemplateDownloadsWhenAuthenticated() throws Exception {
        String token = login("director", "director123");
        String csv = mockMvc.perform(get("/api/import/courses/template")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(csv).contains("courseCode").contains("courseName").contains("semesterCode");
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
