package com.loveofteach.platform.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loveofteach.platform.common.OfferingStatus;
import com.loveofteach.platform.common.ResourceType;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.Course;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.domain.Department;
import com.loveofteach.platform.domain.ResourceChapter;
import com.loveofteach.platform.domain.Semester;
import com.loveofteach.platform.domain.Teacher;
import com.loveofteach.platform.domain.UserAccount;
import com.loveofteach.platform.repository.AuthTokenRepository;
import com.loveofteach.platform.repository.CourseEvaluationRepository;
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.CourseRepository;
import com.loveofteach.platform.repository.DepartmentRepository;
import com.loveofteach.platform.repository.ResourceChapterRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.auth.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:sprint2_test;MODE=MySQL;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Sprint2IntegrationTest {

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
    private SupervisionRecordRepository supervisionRepository;

    @Autowired
    private TeachingResourceRepository resourceRepository;

    @Autowired
    private ResourceChapterRepository chapterRepository;

    @Autowired
    private CourseEvaluationRepository evaluationRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    private Teacher teacherOne;
    private Teacher teacherTwo;
    private Department department;
    private Semester semester;
    private CourseOffering offeringOne;
    private CourseOffering offeringTwo;

    @BeforeEach
    void setUp() {
        authTokenRepository.deleteAll();
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

        department = departmentRepository.save(Department.builder()
                .code("SOFTWARE")
                .name("软件工程教研室")
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
                .department(department)
                .build());
        teacherTwo = teacherRepository.save(Teacher.builder()
                .employeeNo("T002")
                .name("李华")
                .title("讲师")
                .department(department)
                .build());
        Course courseOne = courseRepository.save(Course.builder()
                .code("CS101")
                .name("程序设计基础")
                .credits(new BigDecimal("3.0"))
                .description("程序设计入门课程")
                .department(department)
                .active(true)
                .build());
        Course courseTwo = courseRepository.save(Course.builder()
                .code("CS201")
                .name("数据结构")
                .credits(new BigDecimal("4.0"))
                .description("数据结构核心课程")
                .department(department)
                .active(true)
                .build());
        offeringOne = offeringRepository.save(CourseOffering.builder()
                .course(courseOne)
                .teacher(teacherOne)
                .semester(semester)
                .className("软件2301")
                .studentCount(58)
                .status(OfferingStatus.OPEN)
                .build());
        offeringTwo = offeringRepository.save(CourseOffering.builder()
                .course(courseTwo)
                .teacher(teacherTwo)
                .semester(semester)
                .className("软件2302")
                .studentCount(54)
                .status(OfferingStatus.OPEN)
                .build());

        account("teacher", "张明", "teacher123", UserRole.TEACHER, teacherOne);
        account("teacher2", "李华", "teacher123", UserRole.TEACHER, teacherTwo);
        account("supervisor", "周督导", "supervisor123", UserRole.SUPERVISOR, null);
        account("director", "孟主任", "director123", UserRole.DIRECTOR, null);
    }

    @Test
    void protectedApiRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    @Test
    void loginReturnsIdentityAndRejectsWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"teacher","password":"teacher123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("TEACHER"))
                .andExpect(jsonPath("$.user.teacherId").value(teacherOne.getId()));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"teacher","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationCreatesTeacherIdentity() throws Exception {
        teacherRepository.save(Teacher.builder()
                .employeeNo("T003")
                .name("王芳")
                .title("教授")
                .department(department)
                .build());
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"new_teacher",
                                  "displayName":"李华",
                                  "password":"newpass123",
                                  "role":"TEACHER",
                                  "teacherNo":"T003"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("TEACHER"))
                .andExpect(jsonPath("$.user.teacherNo").value("T003"));
    }

    @Test
    void publicRegistrationOnlyAllowsTeacherRole() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"self_admin",
                                  "displayName":"越权管理员",
                                  "password":"adminpass",
                                  "role":"SUPERVISOR"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("公开注册仅允许教师角色，其他角色由管理员创建"));
    }

    @Test
    void nonAdminCannotModifyCatalogOrImportCourses() throws Exception {
        String token = login("teacher", "teacher123");
        mockMvc.perform(post("/api/semesters")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"2026-2027-2",
                                  "name":"越权学期",
                                  "startDate":"2027-02-20",
                                  "endDate":"2027-07-01",
                                  "active":true
                                }
                                """))
                .andExpect(status().isForbidden());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "courses.csv",
                "text/csv",
                "courseCode,courseName\n".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/import/courses")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherCanUploadResourceWithChapterAndStudentsCanSearchIt() throws Exception {
        String token = login("teacher", "teacher123");
        String chapterBody = mockMvc.perform(post("/api/offerings/{id}/chapters", offeringOne.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"第 1 章 基础语法","displayOrder":1,"description":"变量与流程控制"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long chapterId = objectMapper.readTree(chapterBody).get("id").asLong();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lesson-plan.pdf",
                "application/pdf",
                "%PDF-1.4\nlesson plan".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/offerings/{id}/resources", offeringOne.getId())
                        .file(file)
                        .param("name", "第一章教案")
                        .param("resourceType", "DOCUMENT")
                        .param("chapterId", Long.toString(chapterId))
                        .param("description", "课堂教案")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chapterId").value(chapterId))
                .andExpect(jsonPath("$.originalFilename").value("lesson-plan.pdf"));

        mockMvc.perform(get("/api/courses/{id}", offeringOne.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chapters.length()").value(1))
                .andExpect(jsonPath("$.resources[0].chapterTitle").value("第 1 章 基础语法"));

        mockMvc.perform(get("/api/resources/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("keyword", "教案"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }

    @Test
    void uploadRejectsFileContentThatDoesNotMatchExtension() throws Exception {
        String token = login("teacher", "teacher123");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.pdf",
                "application/pdf",
                "not a real pdf".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/offerings/{id}/resources", offeringOne.getId())
                        .file(file)
                        .param("name", "伪造文件")
                        .param("resourceType", "DOCUMENT")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("文件内容与扩展名不匹配")));
    }

    @Test
    void teacherCannotUploadToAnotherTeachersCourse() throws Exception {
        String token = login("teacher", "teacher123");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "notes".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/offerings/{id}/resources", offeringTwo.getId())
                        .file(file)
                        .param("name", "越权资源")
                        .param("resourceType", "DOCUMENT")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void supervisorEvaluationUpdatesSummaryAndTeacherFeedback() throws Exception {
        String supervisorToken = login("supervisor", "supervisor123");
        mockMvc.perform(post("/api/evaluations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(supervisorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"offeringId":%d,"score":88,"comment":"课堂组织清晰，互动充分"}
                                """.formatted(offeringOne.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(88));

        mockMvc.perform(get("/api/evaluations/summary")
                        .header(HttpHeaders.AUTHORIZATION, bearer(supervisorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluationCount").value(1))
                .andExpect(jsonPath("$.overallAverageScore").value(88.0))
                .andExpect(jsonPath("$.items[0].weak").value(false));

        mockMvc.perform(get("/api/courses/{id}", offeringOne.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(supervisorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supervised").value(true))
                .andExpect(jsonPath("$.supervisionCount").value(1));

        String directorToken = login("director", "director123");
        mockMvc.perform(post("/api/evaluations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(directorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"offeringId":%d,"score":75,"comment":"其他教师课程评价"}
                                """.formatted(offeringTwo.getId())))
                .andExpect(status().isOk());

        String teacherToken = login("teacher", "teacher123");
        mockMvc.perform(get("/api/evaluations/mine")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherId").value(teacherOne.getId()))
                .andExpect(jsonPath("$.evaluationCount").value(1))
                .andExpect(jsonPath("$.feedback.length()").value(1))
                .andExpect(jsonPath("$.feedback[0].comment").value("课堂组织清晰，互动充分"));
    }

    @Test
    void teacherCannotSubmitEvaluation() throws Exception {
        String token = login("teacher", "teacher123");
        mockMvc.perform(post("/api/evaluations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"offeringId":%d,"score":90,"comment":"越权评价"}
                                """.formatted(offeringTwo.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void directorCanExportOfferingStatistics() throws Exception {
        String token = login("director", "director123");
        String csv = mockMvc.perform(get("/api/reports/offerings/export")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        org.hamcrest.Matchers.containsString("course-offering-report")))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(csv).contains("课程代码,课程名称");
        assertThat(csv).contains("CS101");
        assertThat(csv).contains("张明");
    }

    @Test
    void reportEscapesSpreadsheetFormulaInjection() throws Exception {
        Course formulaCourse = courseRepository.save(Course.builder()
                .code("XLS1")
                .name("=1+1")
                .credits(new BigDecimal("1.0"))
                .description("公式注入测试")
                .department(department)
                .active(true)
                .build());
        offeringRepository.save(CourseOffering.builder()
                .course(formulaCourse)
                .teacher(teacherOne)
                .semester(semester)
                .className("软件2399")
                .studentCount(10)
                .status(OfferingStatus.OPEN)
                .build());

        String csv = mockMvc.perform(get("/api/reports/offerings/export")
                        .header(HttpHeaders.AUTHORIZATION,
                                bearer(login("director", "director123"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(csv).contains("'=1+1");
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

    private void account(
            String username,
            String displayName,
            String password,
            UserRole role,
            Teacher teacher
    ) {
        userAccountRepository.save(UserAccount.builder()
                .username(username)
                .displayName(displayName)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .teacher(teacher)
                .active(true)
                .build());
    }
}
