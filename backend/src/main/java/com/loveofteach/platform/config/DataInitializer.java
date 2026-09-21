package com.loveofteach.platform.config;

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
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 在空数据库中写入 Sprint 1 演示数据，便于首次启动直接验证查询和统计功能。
 * 已存在院系数据时跳过初始化，避免覆盖实际数据。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements ApplicationRunner {

    private final DepartmentRepository departmentRepository;
    private final SemesterRepository semesterRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final CourseOfferingRepository offeringRepository;
    private final TeachingResourceRepository resourceRepository;
    private final SupervisionRecordRepository supervisionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (departmentRepository.count() > 0) {
            return;
        }

        Department software = departmentRepository.save(Department.builder()
                .code("SOFTWARE")
                .name("软件工程教研室")
                .active(true)
                .build());
        Department computer = departmentRepository.save(Department.builder()
                .code("COMPUTER")
                .name("计算机科学教研室")
                .active(true)
                .build());
        Department data = departmentRepository.save(Department.builder()
                .code("DATA")
                .name("数据科学教研室")
                .active(true)
                .build());

        Semester spring = semesterRepository.save(Semester.builder()
                .code("2025-2026-2")
                .name("2025-2026 学年第二学期")
                .startDate(LocalDate.of(2026, 2, 23))
                .endDate(LocalDate.of(2026, 7, 10))
                .active(true)
                .build());
        Semester autumn = semesterRepository.save(Semester.builder()
                .code("2026-2027-1")
                .name("2026-2027 学年第一学期")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 1, 15))
                .active(true)
                .build());
        semesterRepository.save(Semester.builder()
                .code("2025-2026-1")
                .name("2025-2026 学年第一学期")
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2026, 1, 15))
                .active(false)
                .build());

        Teacher zhang = teacher(software, "T001", "张明", "副教授");
        Teacher li = teacher(software, "T002", "李华", "讲师");
        Teacher wang = teacher(software, "T003", "王芳", "教授");
        Teacher chen = teacher(software, "T004", "陈晨", "讲师");
        Teacher liu = teacher(software, "T005", "刘洋", "副教授");
        Teacher zhao = teacher(computer, "T006", "赵鑫", "副教授");
        Teacher sun = teacher(data, "T007", "孙悦", "讲师");

        Course programming = course(software, "CS101", "程序设计基础", "3.0",
                "面向软件工程专业一年级学生，讲授程序设计方法、基本语法、结构化与面向对象思想。");
        Course dataStructure = course(software, "CS201", "数据结构", "4.0",
                "覆盖线性表、树、图、查找与排序等核心数据结构及其算法实现。");
        Course softwareEngineering = course(software, "SE301", "软件工程", "3.0",
                "讲解软件过程、需求分析、系统设计、测试与项目质量管理。");
        Course webDevelopment = course(software, "SE302", "Web 应用开发", "3.0",
                "以工程化方式完成前端、后端、数据库和接口联调。");
        Course database = course(software, "CS401", "数据库原理", "4.0",
                "介绍关系模型、SQL、事务、索引和数据库设计方法。");
        Course softwareTesting = course(software, "SE210", "软件测试", "2.5",
                "覆盖测试设计、自动化测试、缺陷管理与质量度量。");
        Course computerNetwork = course(software, "CS403", "计算机网络", "3.5",
                "讲解网络体系结构、传输协议、路由和网络应用。");
        Course operatingSystem = course(software, "CS402", "操作系统", "4.0",
                "覆盖进程、线程、内存、文件系统和并发控制。");
        Course dataAnalysis = course(data, "DS101", "数据分析导论", "3.0",
                "使用统计分析与可视化方法解决业务问题。");
        Course artificialIntelligence = course(computer, "AI101", "人工智能基础", "3.0",
                "介绍搜索、知识表示、机器学习和智能系统基础。");

        CourseOffering p1 = offering(programming, zhang, spring, "软件2301", 58, OfferingStatus.OPEN);
        CourseOffering p2 = offering(programming, zhang, autumn, "软件2401", 61, OfferingStatus.PLANNED);
        CourseOffering d1 = offering(dataStructure, li, spring, "软件2302", 54, OfferingStatus.OPEN);
        CourseOffering d2 = offering(dataStructure, li, autumn, "软件2402", 57, OfferingStatus.PLANNED);
        CourseOffering s1 = offering(softwareEngineering, wang, spring, "软件2201", 47, OfferingStatus.OPEN);
        CourseOffering s2 = offering(softwareEngineering, wang, autumn, "软件2301", 49, OfferingStatus.PLANNED);
        CourseOffering w1 = offering(webDevelopment, chen, spring, "软件2202", 45, OfferingStatus.OPEN);
        CourseOffering w2 = offering(webDevelopment, chen, autumn, "软件2302", 52, OfferingStatus.PLANNED);
        CourseOffering db1 = offering(database, liu, spring, "软件2203", 51, OfferingStatus.OPEN);
        CourseOffering db2 = offering(database, liu, autumn, "软件2303", 55, OfferingStatus.PLANNED);
        CourseOffering t1 = offering(softwareTesting, chen, spring, "软件2201", 43, OfferingStatus.CLOSED);
        CourseOffering n1 = offering(computerNetwork, zhang, spring, "软件2202", 50, OfferingStatus.OPEN);
        CourseOffering os1 = offering(operatingSystem, wang, spring, "软件2203", 46, OfferingStatus.OPEN);
        offering(dataAnalysis, zhao, spring, "数据2301", 40, OfferingStatus.OPEN);
        offering(artificialIntelligence, sun, spring, "计算机2301", 44, OfferingStatus.OPEN);

        resources(p1, "课程导论与考核说明", ResourceType.DOCUMENT, "https://example.com/course/cs101-intro.pdf", 1);
        resources(p1, "循环与函数课件", ResourceType.SLIDES, "https://example.com/course/cs101-slides.pptx", 2);
        resources(p1, "开发环境配置视频", ResourceType.VIDEO, "https://example.com/course/cs101-video.mp4", 3);
        resources(d1, "线性表与链表讲义", ResourceType.DOCUMENT, "https://example.com/course/cs201-list.pdf", 2);
        resources(s1, "课程整体案例包", ResourceType.LINK, "https://example.com/course/se301-case", 1);
        resources(w1, "接口设计示例", ResourceType.DOCUMENT, "https://example.com/course/se302-api.pdf", 1);
        resources(db1, "SQL 实验手册", ResourceType.DOCUMENT, "https://example.com/course/cs401-lab.pdf", 1);
        resources(t1, "测试用例模板", ResourceType.SLIDES, "https://example.com/course/se210-template.pptx", 1);

        supervise(p1, "校级督导 周老师", LocalDate.of(2026, 3, 12));
        supervise(d1, "院级督导 胡老师", LocalDate.of(2026, 3, 18));
        supervise(s1, "校级督导 周老师", LocalDate.of(2026, 4, 2));
        supervise(db1, "院级督导 胡老师", LocalDate.of(2026, 4, 16));
        supervise(t1, "院级督导 胡老师", LocalDate.of(2026, 5, 8));
        supervise(n1, "校级督导 周老师", LocalDate.of(2026, 5, 20));
    }

    private Teacher teacher(Department department, String no, String name, String title) {
        return teacherRepository.save(Teacher.builder()
                .employeeNo(no)
                .name(name)
                .title(title)
                .department(department)
                .build());
    }

    private Course course(Department department, String code, String name, String credits, String description) {
        return courseRepository.save(Course.builder()
                .code(code)
                .name(name)
                .credits(new BigDecimal(credits))
                .description(description)
                .department(department)
                .active(true)
                .build());
    }

    private CourseOffering offering(
            Course course,
            Teacher teacher,
            Semester semester,
            String className,
            int studentCount,
            OfferingStatus status
    ) {
        return offeringRepository.save(CourseOffering.builder()
                .course(course)
                .teacher(teacher)
                .semester(semester)
                .className(className)
                .studentCount(studentCount)
                .status(status)
                .build());
    }

    private void resources(
            CourseOffering offering,
            String name,
            ResourceType type,
            String url,
            int daysAgo
    ) {
        resourceRepository.save(TeachingResource.builder()
                .offering(offering)
                .name(name)
                .resourceType(type)
                .url(url)
                .uploadedAt(LocalDateTime.now().minusDays(daysAgo))
                .build());
    }

    private void supervise(CourseOffering offering, String supervisor, LocalDate date) {
        supervisionRepository.save(SupervisionRecord.builder()
                .offering(offering)
                .supervisor(supervisor)
                .supervisedAt(date)
                .build());
    }
}
