package com.loveofteach.platform.api;

import com.loveofteach.platform.common.OfferingStatus;
import com.loveofteach.platform.common.ResourceType;
import com.loveofteach.platform.domain.Course;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.domain.Department;
import com.loveofteach.platform.domain.Semester;
import com.loveofteach.platform.domain.SupervisionRecord;
import com.loveofteach.platform.domain.Teacher;
import com.loveofteach.platform.domain.TeachingResource;
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.CourseRepository;
import com.loveofteach.platform.repository.DepartmentRepository;
import com.loveofteach.platform.repository.SemesterRepository;
import com.loveofteach.platform.repository.SupervisionRecordRepository;
import com.loveofteach.platform.repository.TeacherRepository;
import com.loveofteach.platform.repository.TeachingResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CoursePlatformIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

    private Department software;
    private Semester spring;
    private Semester autumn;
    private List<Teacher> softwareTeachers;
    private Course programming;
    private Course dataStructure;
    private CourseOffering programmingSpring;
    private CourseOffering dataStructureSpring;

    @BeforeEach
    void setUp() {
        supervisionRepository.deleteAll();
        resourceRepository.deleteAll();
        offeringRepository.deleteAll();
        courseRepository.deleteAll();
        teacherRepository.deleteAll();
        semesterRepository.deleteAll();
        departmentRepository.deleteAll();

        software = departmentRepository.save(Department.builder()
                .code("SOFTWARE")
                .name("软件工程教研室")
                .active(true)
                .build());
        Department otherDepartment = departmentRepository.save(Department.builder()
                .code("DATA")
                .name("数据科学教研室")
                .active(true)
                .build());
        spring = semesterRepository.save(Semester.builder()
                .code("2025-2026-2")
                .name("2025-2026 学年第二学期")
                .startDate(LocalDate.of(2026, 2, 23))
                .endDate(LocalDate.of(2026, 7, 10))
                .active(true)
                .build());
        autumn = semesterRepository.save(Semester.builder()
                .code("2026-2027-1")
                .name("2026-2027 学年第一学期")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 1, 15))
                .active(true)
                .build());

        softwareTeachers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            softwareTeachers.add(teacherRepository.save(Teacher.builder()
                    .employeeNo("T00" + i)
                    .name("教师" + i)
                    .title(i % 2 == 0 ? "讲师" : "副教授")
                    .department(software)
                    .build()));
        }
        Teacher otherTeacher = teacherRepository.save(Teacher.builder()
                .employeeNo("T006")
                .name("数据教师")
                .title("讲师")
                .department(otherDepartment)
                .build());

        programming = courseRepository.save(course(
                software, "CS101", "程序设计基础", "3.0", "程序设计入门课程"));
        dataStructure = courseRepository.save(course(
                software, "CS201", "数据结构", "4.0", "核心数据结构课程"));
        Course database = courseRepository.save(course(
                software, "CS401", "数据库原理", "4.0", "数据库课程"));
        Course network = courseRepository.save(course(
                software, "CS403", "计算机网络", "3.5", "网络课程"));
        Course softwareTesting = courseRepository.save(course(
                software, "SE210", "软件测试", "2.5", "测试课程"));
        Course otherCourse = courseRepository.save(course(
                otherDepartment, "DS101", "数据分析导论", "3.0", "数据分析课程"));

        programmingSpring = offeringRepository.save(offering(
                programming, softwareTeachers.get(0), spring, "软件2301", 58, OfferingStatus.OPEN));
        offeringRepository.save(offering(
                programming, softwareTeachers.get(0), autumn, "软件2401", 61, OfferingStatus.PLANNED));
        dataStructureSpring = offeringRepository.save(offering(
                dataStructure, softwareTeachers.get(1), spring, "软件2302", 54, OfferingStatus.OPEN));
        offeringRepository.save(offering(
                dataStructure, softwareTeachers.get(1), autumn, "软件2402", 57, OfferingStatus.PLANNED));
        offeringRepository.save(offering(
                database, softwareTeachers.get(2), spring, "软件2201", 47, OfferingStatus.OPEN));
        offeringRepository.save(offering(
                network, softwareTeachers.get(3), spring, "软件2202", 45, OfferingStatus.OPEN));
        offeringRepository.save(offering(
                softwareTesting, softwareTeachers.get(4), spring, "软件2203", 43, OfferingStatus.CLOSED));
        offeringRepository.save(offering(
                otherCourse, otherTeacher, spring, "数据2301", 40, OfferingStatus.OPEN));

        resourceRepository.save(TeachingResource.builder()
                .offering(programmingSpring)
                .name("课程导论")
                .resourceType(ResourceType.DOCUMENT)
                .url("https://example.com/cs101-intro.pdf")
                .uploadedAt(LocalDateTime.now().minusDays(2))
                .build());
        resourceRepository.save(TeachingResource.builder()
                .offering(programmingSpring)
                .name("循环与函数课件")
                .resourceType(ResourceType.SLIDES)
                .url("https://example.com/cs101-slides.pptx")
                .uploadedAt(LocalDateTime.now().minusDays(1))
                .build());
        supervisionRepository.save(SupervisionRecord.builder()
                .offering(programmingSpring)
                .supervisor("校级督导 周老师")
                .supervisedAt(LocalDate.of(2026, 3, 12))
                .build());
    }

    @Test
    void filtersCourseListBySemester() throws Exception {
        mockMvc.perform(get("/api/courses").param("semesterId", spring.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].semesterId").value(spring.getId()))
                .andExpect(jsonPath("$[0].courseCode").isNotEmpty())
                .andExpect(jsonPath("$[0].resourceCount").value(2));

        mockMvc.perform(get("/api/courses")
                        .param("semesterId", autumn.getId().toString())
                        .param("keyword", "不存在"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void teacherOverviewReturnsFiveTeachersAndCourseData() throws Exception {
        mockMvc.perform(get("/api/courses/teacher-overview")
                        .param("departmentId", software.getId().toString())
                        .param("semesterId", spring.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherCount").value(5))
                .andExpect(jsonPath("$.courseCount").value(5))
                .andExpect(jsonPath("$.teachers.length()").value(5))
                .andExpect(jsonPath("$.teachers[0].courses").isArray())
                .andExpect(jsonPath("$.teachers[0].courses[0].studentCount").isNumber())
                .andExpect(jsonPath("$.teachers[0].courses[0].statusLabel").isNotEmpty());
    }

    @Test
    void coverageCalculatesOfferingsAndRate() throws Exception {
        mockMvc.perform(get("/api/supervision/coverage")
                        .param("semesterId", spring.getId().toString())
                        .param("departmentId", software.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.offeringCount").value(5))
                .andExpect(jsonPath("$.summary.supervisedCount").value(1))
                .andExpect(jsonPath("$.summary.unsupervisedCount").value(4))
                .andExpect(jsonPath("$.summary.coverageRate").value(20.0))
                .andExpect(jsonPath("$.summary.studentCount").value(247))
                .andExpect(jsonPath("$.departmentBreakdown.length()").value(1))
                .andExpect(jsonPath("$.courses.length()").value(5));
    }

    @Test
    void courseDetailContainsStudentCountAndResources() throws Exception {
        mockMvc.perform(get("/api/courses/{id}", programmingSpring.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentCount").value(58))
                .andExpect(jsonPath("$.description").value("程序设计入门课程"))
                .andExpect(jsonPath("$.supervisionCount").value(1))
                .andExpect(jsonPath("$.supervised").value(true))
                .andExpect(jsonPath("$.resources.length()").value(2))
                .andExpect(jsonPath("$.resources[0].resourceTypeLabel").isNotEmpty());
    }

    @Test
    void importReturnsFailuresAndPersistsValidRows() throws Exception {
        String csv = """
                courseCode,courseName,departmentCode,credits,description,teacherNo,teacherName,semesterCode,className,studentCount,status
                CS500,云计算基础,SOFTWARE,3.0,云平台课程,T008,周宁,2025-2026-2,软件2304,48,OPEN
                CS501,错误数据,SOFTWARE,3.0,错误课程,T009,吴迪,2025-2026-2,软件2305,-1,OPEN
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "courses.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/import/courses").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(2))
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failureCount").value(1))
                .andExpect(jsonPath("$.failures[0].rowNumber").value(3))
                .andExpect(jsonPath("$.failures[0].courseCode").value("CS501"))
                .andExpect(jsonPath("$.failures[0].reason").value("学生人数必须是非负整数"))
                .andExpect(jsonPath("$.failureCsv").value(org.hamcrest.Matchers.containsString("CS501")));

        assertThat(courseRepository.findByCodeIgnoreCase("CS500")).isPresent();
        assertThat(courseRepository.findByCodeIgnoreCase("CS501")).isEmpty();
        assertThat(teacherRepository.findByEmployeeNoIgnoreCase("T008")).isPresent();
    }

    @Test
    void importRollsBackAllWritesForFailedRow() throws Exception {
        String csv = """
                courseCode,courseName,departmentCode,credits,description,teacherNo,teacherName,semesterCode,className,studentCount,status
                DS101,错误归属课程,SOFTWARE,3.0,课程已属于其他院系,T099,新教师,2025-2026-2,软件2409,36,OPEN
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "rollback.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/import/courses").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(0))
                .andExpect(jsonPath("$.failureCount").value(1))
                .andExpect(jsonPath("$.failures[0].reason")
                        .value(org.hamcrest.Matchers.containsString("已归属于其他院系")));

        assertThat(teacherRepository.findByEmployeeNoIgnoreCase("T099")).isEmpty();
        assertThat(offeringRepository.findByCourseIdAndTeacherIdAndSemesterIdAndClassName(
                courseRepository.findByCodeIgnoreCase("DS101").orElseThrow().getId(),
                softwareTeachers.get(0).getId(),
                spring.getId(),
                "软件2409")).isEmpty();
    }

    @Test
    void importRejectsInactiveSemester() throws Exception {
        spring.setActive(false);
        semesterRepository.save(spring);
        String csv = """
                courseCode,courseName,departmentCode,credits,description,teacherNo,teacherName,semesterCode,className,studentCount,status
                CS600,停用学期课程,SOFTWARE,3.0,测试课程,T001,教师1,2025-2026-2,软件2408,40,OPEN
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "inactive-semester.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/import/courses").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(0))
                .andExpect(jsonPath("$.failureCount").value(1))
                .andExpect(jsonPath("$.failures[0].reason").value("学期已停用：2025-2026-2"));

        assertThat(courseRepository.findByCodeIgnoreCase("CS600")).isEmpty();
    }

    @Test
    void invalidPathParameterReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/courses/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("格式不正确")));
    }

    @Test
    void catalogSupportsCreateAndDeactivate() throws Exception {
        mockMvc.perform(post("/api/semesters")
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
                .andExpect(jsonPath("$.code").value("2027-2028-1"))
                .andExpect(jsonPath("$.active").value(true));

        Long semesterId = semesterRepository.findByCodeIgnoreCase("2027-2028-1").orElseThrow().getId();
        mockMvc.perform(patch("/api/semesters/{id}/status", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void duplicateDepartmentCodeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "SOFTWARE",
                                  "name": "重复院系",
                                  "active": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("已存在")));
    }

    private Teacher teacher(
            Department department,
            String no,
            String name,
            String title
    ) {
        return Teacher.builder()
                .employeeNo(no)
                .name(name)
                .title(title)
                .department(department)
                .build();
    }

    private Course course(
            Department department,
            String code,
            String name,
            String credits,
            String description
    ) {
        return Course.builder()
                .code(code)
                .name(name)
                .credits(new BigDecimal(credits))
                .description(description)
                .department(department)
                .active(true)
                .build();
    }

    private CourseOffering offering(
            Course course,
            Teacher teacher,
            Semester semester,
            String className,
            int studentCount,
            OfferingStatus status
    ) {
        return CourseOffering.builder()
                .course(course)
                .teacher(teacher)
                .semester(semester)
                .className(className)
                .studentCount(studentCount)
                .status(status)
                .build();
    }
}
