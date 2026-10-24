package com.loveofteach.platform.config;

import com.loveofteach.platform.common.OfferingStatus;
import com.loveofteach.platform.common.ResourceQualityLevel;
import com.loveofteach.platform.common.ResourceType;
import com.loveofteach.platform.common.SupervisionPlanStatus;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.Course;
import com.loveofteach.platform.domain.CourseEvaluation;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.domain.CourseSubscription;
import com.loveofteach.platform.domain.Department;
import com.loveofteach.platform.domain.ResourceChapter;
import com.loveofteach.platform.domain.ResourceQualityReview;
import com.loveofteach.platform.domain.Semester;
import com.loveofteach.platform.domain.SupervisionPlan;
import com.loveofteach.platform.domain.SupervisionRecord;
import com.loveofteach.platform.domain.Teacher;
import com.loveofteach.platform.domain.TeachingResource;
import com.loveofteach.platform.domain.UserAccount;
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.CourseEvaluationRepository;
import com.loveofteach.platform.repository.CourseNotificationRepository;
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
import com.loveofteach.platform.service.CourseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataInitializer implements ApplicationRunner {

    private final DepartmentRepository departmentRepository;
    private final SemesterRepository semesterRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final CourseOfferingRepository offeringRepository;
    private final TeachingResourceRepository resourceRepository;
    private final SupervisionRecordRepository supervisionRepository;
    private final ResourceChapterRepository chapterRepository;
    private final CourseEvaluationRepository evaluationRepository;
    private final CourseNotificationRepository notificationRepository;
    private final CourseSubscriptionRepository subscriptionRepository;
    private final SupervisionPlanRepository supervisionPlanRepository;
    private final ResourceQualityReviewRepository qualityReviewRepository;
    private final CourseNotificationService notificationService;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (departmentRepository.count() > 0) {
            ensureAccounts();
            seedSprint2Samples();
            seedSprint3Samples();
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

        ensureAccounts();
        seedSprint2Samples();
        seedSprint3Samples();
    }

    private void ensureAccounts() {
        if (userAccountRepository.count() > 0) {
            return;
        }
        Teacher zhang = teacherRepository.findByEmployeeNoIgnoreCase("T001").orElse(null);
        Teacher li = teacherRepository.findByEmployeeNoIgnoreCase("T002").orElse(null);
        account("director", "孟主任", "director123", UserRole.DIRECTOR, null);
        account("supervisor", "周督导", "supervisor123", UserRole.SUPERVISOR, null);
        account("teacher", "张明", "teacher123", UserRole.TEACHER, zhang);
        account("teacher2", "李华", "teacher123", UserRole.TEACHER, li);
        account("admin", "系统管理员", "admin123", UserRole.ADMIN, null);
    }

    private void seedSprint2Samples() {
        List<CourseOffering> offerings = offeringRepository.search(null, null, null, null);
        if (offerings.isEmpty()) {
            return;
        }
        CourseOffering first = findOffering(offerings, "CS101", "2025-2026-2").orElse(offerings.get(0));
        CourseOffering second = findOffering(offerings, "CS201", "2025-2026-2")
                .orElse(offerings.get(Math.min(1, offerings.size() - 1)));
        if (chapterRepository.count() == 0) {
            ResourceChapter chapterOne = chapterRepository.save(ResourceChapter.builder()
                    .offering(first)
                    .title("第 1 章 课程导论")
                    .displayOrder(1)
                    .description("课程目标、开发环境与学习要求")
                    .build());
            ResourceChapter chapterTwo = chapterRepository.save(ResourceChapter.builder()
                    .offering(first)
                    .title("第 2 章 函数与模块")
                    .displayOrder(2)
                    .description("函数抽象、模块化与复用")
                    .build());
            List<TeachingResource> firstResources =
                    resourceRepository.findByOfferingIdOrderByUploadedAtDesc(first.getId());
            for (int index = 0; index < firstResources.size(); index++) {
                TeachingResource resource = firstResources.get(index);
                resource.setChapter(index % 2 == 0 ? chapterOne : chapterTwo);
                resourceRepository.save(resource);
            }
            ResourceChapter secondChapter = chapterRepository.save(ResourceChapter.builder()
                    .offering(second)
                    .title("第 1 章 线性表")
                    .displayOrder(1)
                    .description("顺序表与链表")
                    .build());
            resourceRepository.findByOfferingIdOrderByUploadedAtDesc(second.getId()).stream()
                    .findFirst()
                    .ifPresent(resource -> {
                        resource.setChapter(secondChapter);
                        resourceRepository.save(resource);
                    });
        }
        if (evaluationRepository.count() == 0) {
            UserAccount supervisor = userAccountRepository.findByUsernameIgnoreCase("supervisor").orElse(null);
            UserAccount director = userAccountRepository.findByUsernameIgnoreCase("director").orElse(null);
            if (supervisor != null) {
                evaluation(first, supervisor, 86, "课堂组织清晰，案例与课程目标匹配。");
            }
            if (director != null) {
                evaluation(second, director, 68, "课堂互动偏少，建议增加分层练习与随堂反馈。");
            }
        }
    }

    private void seedSprint3Samples() {
        List<CourseOffering> offerings = offeringRepository.search(null, null, null, null);
        if (offerings.isEmpty()) {
            return;
        }
        UserAccount supervisor = userAccountRepository
                .findByUsernameIgnoreCase("supervisor")
                .orElse(null);
        UserAccount teacher = userAccountRepository
                .findByUsernameIgnoreCase("teacher")
                .orElse(null);
        CourseOffering first = findOffering(offerings, "CS101", "2025-2026-2")
                .orElse(offerings.get(0));
        CourseOffering second = findOffering(offerings, "CS201", "2025-2026-2")
                .orElse(offerings.get(Math.min(1, offerings.size() - 1)));

        if (subscriptionRepository.count() == 0
                && teacher != null
                && teacher.getTeacher() != null) {
            Course course = first.getCourse();
            if (offeringRepository.existsByCourseIdAndTeacherId(
                    course.getId(), teacher.getTeacher().getId())) {
                subscriptionRepository.save(CourseSubscription.builder()
                        .teacher(teacher)
                        .course(course)
                        .active(true)
                        .build());
            }
        }
        if (notificationRepository.count() == 0) {
            subscriptionRepository.findByCourseIdAndActiveTrue(first.getCourse().getId())
                    .stream()
                    .findFirst()
                    .ifPresent(subscription -> notificationService.notifyCourseChanged(
                            first.getCourse(),
                            "课程 CS101 的开课信息已更新，请查看最新班级、人数和状态。"));
        }

        if (qualityReviewRepository.count() == 0 && supervisor != null) {
            List<TeachingResource> resources =
                    resourceRepository.findByOfferingIdOrderByUploadedAtDesc(first.getId());
            if (!resources.isEmpty()) {
                qualityReviewRepository.save(ResourceQualityReview.builder()
                        .resource(resources.get(0))
                        .reviewer(supervisor)
                        .level(ResourceQualityLevel.EXCELLENT)
                        .comment("章节结构清晰，案例与课程目标匹配，推荐作为示范资源。")
                        .recommended(true)
                        .reviewedAt(LocalDateTime.now().minusDays(1))
                        .build());
            }
        }

        if (supervisionPlanRepository.count() == 0 && supervisor != null) {
            LocalDate today = LocalDate.now();
            supervisionPlanRepository.save(SupervisionPlan.builder()
                    .offering(first)
                    .supervisor(supervisor)
                    .plannedDate(today.plusDays(2))
                    .dueDate(today.plusDays(5))
                    .status(SupervisionPlanStatus.PLANNED)
                    .note("检查课堂互动与章节资源使用情况")
                    .build());
            supervisionPlanRepository.save(SupervisionPlan.builder()
                    .offering(second)
                    .supervisor(supervisor)
                    .plannedDate(today)
                    .dueDate(today.plusDays(7))
                    .status(SupervisionPlanStatus.IN_PROGRESS)
                    .note("跟踪低覆盖课程资源补充进度")
                    .build());
        }

        boolean hasInactiveResource = resourceRepository
                .findByOfferingIdOrderByUploadedAtDesc(first.getId())
                .stream()
                .anyMatch(resource -> !resource.isActive());
        if (!hasInactiveResource) {
            resourceRepository.findByOfferingIdOrderByUploadedAtDesc(first.getId())
                    .stream()
                    .filter(TeachingResource::isActive)
                    .skip(1)
                    .findFirst()
                    .ifPresent(resource -> {
                        resource.setActive(false);
                        resource.setUnavailableAt(LocalDateTime.now().minusDays(1));
                        resource.setUnavailableReason("课程内容已更新，等待教师确认后恢复");
                        resourceRepository.save(resource);
                    });
        }
    }

    private java.util.Optional<CourseOffering> findOffering(
            List<CourseOffering> offerings,
            String courseCode,
            String semesterCode
    ) {
        return offerings.stream()
                .filter(offering -> offering.getCourse().getCode().equals(courseCode))
                .filter(offering -> offering.getSemester().getCode().equals(semesterCode))
                .findFirst();
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

    private void evaluation(
            CourseOffering offering,
            UserAccount reviewer,
            int score,
            String comment
    ) {
        evaluationRepository.save(CourseEvaluation.builder()
                .offering(offering)
                .reviewer(reviewer)
                .score(score)
                .comment(comment)
                .evaluatedAt(LocalDateTime.now().minusDays(3))
                .build());
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
