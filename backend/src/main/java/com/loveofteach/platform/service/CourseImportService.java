package com.loveofteach.platform.service;

import com.loveofteach.platform.api.dto.ImportFailureResponse;
import com.loveofteach.platform.api.dto.ImportResultResponse;
import com.loveofteach.platform.common.BusinessException;
import com.loveofteach.platform.common.CsvEscaper;
import com.loveofteach.platform.common.OfferingStatus;
import com.loveofteach.platform.domain.Course;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.domain.Department;
import com.loveofteach.platform.domain.Semester;
import com.loveofteach.platform.domain.Teacher;
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.CourseRepository;
import com.loveofteach.platform.repository.DepartmentRepository;
import com.loveofteach.platform.repository.SemesterRepository;
import com.loveofteach.platform.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 解析课程与教师 CSV，并逐行校验、写入基础数据。
 * 每一行使用独立事务，失败行回滚，成功行继续保留。
 */
@Service
@RequiredArgsConstructor
public class CourseImportService {

    public static final List<String> TEMPLATE_HEADERS = List.of(
            "courseCode",
            "courseName",
            "departmentCode",
            "credits",
            "description",
            "teacherNo",
            "teacherName",
            "semesterCode",
            "className",
            "studentCount",
            "status");

    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build();

    private final DepartmentRepository departmentRepository;
    private final SemesterRepository semesterRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final CourseOfferingRepository offeringRepository;
    private final TransactionTemplate transactionTemplate;
    private final CourseNotificationService notificationService;

    public byte[] buildTemplate() {
        String csv = String.join(",", TEMPLATE_HEADERS) + System.lineSeparator()
                + "CS101,程序设计基础,SOFTWARE,3.0,课程简介,"
                + "T001,张明,2025-2026-2,软件2301,56,OPEN"
                + System.lineSeparator();
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    public ImportResultResponse importCourses(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择需要导入的 CSV 文件");
        }

        List<ImportFailureResponse> failures = new ArrayList<>();
        int totalRows = 0;
        int successCount = 0;

        try (Reader reader = new java.io.InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVParser.parse(reader, CSV_FORMAT)) {
            for (CSVRecord record : parser) {
                totalRows++;
                long rowNumber = record.getRecordNumber() + 1;
                Map<String, String> row = normalize(record.toMap());
                try {
                    // A failed row must not leave partially written teachers, courses, or offerings.
                    transactionTemplate.executeWithoutResult(status -> importRow(row));
                    successCount++;
                } catch (BusinessException ex) {
                    failures.add(new ImportFailureResponse(rowNumber, row.get("courseCode"), ex.getMessage()));
                } catch (RuntimeException ex) {
                    failures.add(new ImportFailureResponse(
                            rowNumber,
                            row.get("courseCode"),
                            "数据保存失败：" + rootMessage(ex)));
                }
            }
        } catch (IOException ex) {
            throw new BusinessException("CSV 文件读取失败：" + ex.getMessage());
        }

        return new ImportResultResponse(
                totalRows,
                successCount,
                failures.size(),
                List.copyOf(failures),
                buildFailureCsv(failures));
    }

    private void importRow(Map<String, String> row) {
        String courseCode = required(row, "courseCode", "课程代码");
        String courseName = required(row, "courseName", "课程名称");
        String departmentCode = required(row, "departmentCode", "院系编码");
        String teacherNo = required(row, "teacherNo", "教师工号");
        String teacherName = required(row, "teacherName", "教师姓名");
        String semesterCode = required(row, "semesterCode", "学期编码");
        String className = required(row, "className", "班级");
        int studentCount = parseNonNegativeInt(required(row, "studentCount", "学生人数"), "学生人数");
        OfferingStatus status = OfferingStatus.from(required(row, "status", "开课状态"));
        BigDecimal credits = parseCredits(required(row, "credits", "学分"));

        Department department = departmentRepository.findByCodeIgnoreCase(departmentCode)
                .orElseThrow(() -> new BusinessException("院系编码不存在：" + departmentCode));
        if (!department.isActive()) {
            throw new BusinessException("院系已停用：" + departmentCode);
        }
        Semester semester = semesterRepository.findByCodeIgnoreCase(semesterCode)
                .orElseThrow(() -> new BusinessException("学期编码不存在：" + semesterCode));
        if (!semester.isActive()) {
            throw new BusinessException("学期已停用：" + semesterCode);
        }

        Teacher teacher = teacherRepository.findByEmployeeNoIgnoreCase(teacherNo)
                .map(existing -> updateTeacher(existing, teacherName, department))
                .orElseGet(() -> teacherRepository.save(Teacher.builder()
                        .employeeNo(teacherNo)
                        .name(teacherName)
                        .title("教师")
                        .department(department)
                        .build()));

        Course existingCourse = courseRepository.findByCodeIgnoreCase(courseCode).orElse(null);
        boolean courseChanged = existingCourse == null
                || !Objects.equals(existingCourse.getName(), courseName)
                || existingCourse.getCredits().compareTo(credits) != 0
                || !Objects.equals(
                        defaultString(existingCourse.getDescription()),
                        defaultString(row.get("description")))
                || !existingCourse.getDepartment().getId().equals(department.getId())
                || !existingCourse.isActive();
        Course course = java.util.Optional.ofNullable(existingCourse)
                .map(existing -> updateCourse(existing, courseName, credits, row.get("description"), department))
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .code(courseCode)
                        .name(courseName)
                        .credits(credits)
                        .description(defaultString(row.get("description")))
                        .department(department)
                        .active(true)
                        .build()));

        CourseOffering existingOffering = offeringRepository
                .findByCourseIdAndTeacherIdAndSemesterIdAndClassName(
                        course.getId(), teacher.getId(), semester.getId(), className)
                .orElse(null);
        boolean offeringChanged = existingOffering == null
                || existingOffering.getStudentCount() != studentCount
                || existingOffering.getStatus() != status;
        CourseOffering offering = java.util.Optional.ofNullable(existingOffering)
                .orElseGet(() -> CourseOffering.builder()
                        .course(course)
                        .teacher(teacher)
                        .semester(semester)
                        .className(className)
                        .studentCount(studentCount)
                        .status(status)
                        .build());
        offering.setStudentCount(studentCount);
        offering.setStatus(status);
        offeringRepository.save(offering);
        if (courseChanged || offeringChanged) {
            notificationService.notifyCourseChanged(
                    course,
                    "课程 " + course.getCode()
                            + " 的开课信息已更新，请查看最新班级、人数和状态。");
        }
    }

    private Course updateCourse(
            Course course,
            String courseName,
            BigDecimal credits,
            String description,
            Department department
    ) {
        if (!course.getDepartment().getId().equals(department.getId())) {
            throw new BusinessException("课程 " + course.getCode() + " 已归属于其他院系");
        }
        course.setName(courseName);
        course.setCredits(credits);
        course.setDescription(defaultString(description));
        course.setActive(true);
        return courseRepository.save(course);
    }

    private Teacher updateTeacher(Teacher teacher, String teacherName, Department department) {
        if (!teacher.getDepartment().getId().equals(department.getId())) {
            throw new BusinessException("教师 " + teacher.getEmployeeNo() + " 已归属于其他院系");
        }
        teacher.setName(teacherName);
        return teacherRepository.save(teacher);
    }

    private Map<String, String> normalize(Map<String, String> source) {
        Map<String, String> normalized = new LinkedHashMap<>();
        source.forEach((key, value) -> normalized.put(
                key.replace("\uFEFF", "").trim(),
                value == null ? "" : value.trim()));
        return normalized;
    }

    private String required(Map<String, String> row, String key, String label) {
        String value = row.get(key);
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(label + "不能为空");
        }
        return value.trim();
    }

    private int parseNonNegativeInt(String value, String label) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 0) {
                throw new BusinessException(label + "必须是非负整数");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new BusinessException(label + "必须是非负整数");
        }
    }

    private BigDecimal parseCredits(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException("学分不能为空");
        }
        try {
            BigDecimal credits = new BigDecimal(value.trim());
            if (credits.signum() < 0) {
                throw new BusinessException("学分必须是非负数字");
            }
            return credits.setScale(1, java.math.RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new BusinessException("学分必须是非负数字");
        }
    }

    private String defaultString(String value) {
        return value == null ? "" : value.trim();
    }

    private String buildFailureCsv(List<ImportFailureResponse> failures) {
        StringWriter writer = new StringWriter();
        writer.write("rowNumber,courseCode,reason");
        writer.write(System.lineSeparator());
        for (ImportFailureResponse failure : failures) {
            writer.write(String.join(",",
                    Long.toString(failure.rowNumber()),
                    CsvEscaper.escape(failure.courseCode()),
                    CsvEscaper.escape(failure.reason())));
            writer.write(System.lineSeparator());
        }
        return writer.toString();
    }

    private String rootMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String message = root.getMessage();
        return StringUtils.hasText(message) ? message : root.getClass().getSimpleName();
    }
}
