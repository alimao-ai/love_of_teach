package com.loveofteach.platform.service;

import com.loveofteach.platform.api.dto.DepartmentRequest;
import com.loveofteach.platform.api.dto.DepartmentResponse;
import com.loveofteach.platform.api.dto.SemesterRequest;
import com.loveofteach.platform.api.dto.SemesterResponse;
import com.loveofteach.platform.api.dto.TeacherResponse;
import com.loveofteach.platform.common.BusinessException;
import com.loveofteach.platform.domain.Department;
import com.loveofteach.platform.domain.Semester;
import com.loveofteach.platform.domain.Teacher;
import com.loveofteach.platform.repository.DepartmentRepository;
import com.loveofteach.platform.repository.SemesterRepository;
import com.loveofteach.platform.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 维护课程查询依赖的院系、学期和教师基础数据。
 * 停用操作只改变业务可用状态，不删除已有课程引用。
 */
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final DepartmentRepository departmentRepository;
    private final SemesterRepository semesterRepository;
    private final TeacherRepository teacherRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listDepartments(boolean activeOnly) {
        List<Department> departments = activeOnly
                ? departmentRepository.findByActiveTrueOrderByNameAsc()
                : departmentRepository.findAllByOrderByNameAsc();
        return departments.stream().map(this::toDepartmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(Long id) {
        return toDepartmentResponse(requireDepartment(id));
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByCodeIgnoreCase(request.code().trim())) {
            throw new BusinessException("院系编码已存在：" + request.code());
        }
        Department department = Department.builder()
                .code(request.code().trim())
                .name(request.name().trim())
                .active(request.active())
                .build();
        return toDepartmentResponse(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = requireDepartment(id);
        String code = request.code().trim();
        if (departmentRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BusinessException("院系编码已存在：" + code);
        }
        department.setCode(code);
        department.setName(request.name().trim());
        department.setActive(request.active());
        return toDepartmentResponse(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse updateDepartmentStatus(Long id, boolean active) {
        Department department = requireDepartment(id);
        department.setActive(active);
        return toDepartmentResponse(departmentRepository.save(department));
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> listSemesters(boolean activeOnly) {
        List<Semester> semesters = activeOnly
                ? semesterRepository.findByActiveTrueOrderByStartDateDesc()
                : semesterRepository.findAllByOrderByStartDateDesc();
        return semesters.stream().map(this::toSemesterResponse).toList();
    }

    @Transactional(readOnly = true)
    public SemesterResponse getSemester(Long id) {
        return toSemesterResponse(requireSemester(id));
    }

    @Transactional
    public SemesterResponse createSemester(SemesterRequest request) {
        validateSemesterDates(request);
        if (semesterRepository.existsByCodeIgnoreCase(request.code().trim())) {
            throw new BusinessException("学期编码已存在：" + request.code());
        }
        Semester semester = Semester.builder()
                .code(request.code().trim())
                .name(request.name().trim())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .active(request.active())
                .build();
        return toSemesterResponse(semesterRepository.save(semester));
    }

    @Transactional
    public SemesterResponse updateSemester(Long id, SemesterRequest request) {
        validateSemesterDates(request);
        Semester semester = requireSemester(id);
        String code = request.code().trim();
        if (semesterRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BusinessException("学期编码已存在：" + code);
        }
        semester.setCode(code);
        semester.setName(request.name().trim());
        semester.setStartDate(request.startDate());
        semester.setEndDate(request.endDate());
        semester.setActive(request.active());
        return toSemesterResponse(semesterRepository.save(semester));
    }

    @Transactional
    public SemesterResponse updateSemesterStatus(Long id, boolean active) {
        Semester semester = requireSemester(id);
        semester.setActive(active);
        return toSemesterResponse(semesterRepository.save(semester));
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> listTeachers(Long departmentId) {
        List<Teacher> teachers = departmentId == null
                ? teacherRepository.findAllByOrderByNameAsc()
                : teacherRepository.findByDepartmentIdOrderByNameAsc(departmentId);
        return teachers.stream().map(this::toTeacherResponse).toList();
    }

    private Department requireDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("院系不存在", HttpStatus.NOT_FOUND));
    }

    private Semester requireSemester(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new BusinessException("学期不存在", HttpStatus.NOT_FOUND));
    }

    private void validateSemesterDates(SemesterRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException("学期结束日期不能早于开始日期");
        }
    }

    private DepartmentResponse toDepartmentResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getCode(),
                department.getName(),
                department.isActive(),
                teacherRepository.countByDepartmentId(department.getId()));
    }

    private SemesterResponse toSemesterResponse(Semester semester) {
        return new SemesterResponse(
                semester.getId(),
                semester.getCode(),
                semester.getName(),
                semester.getStartDate(),
                semester.getEndDate(),
                semester.isActive());
    }

    private TeacherResponse toTeacherResponse(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getEmployeeNo(),
                teacher.getName(),
                teacher.getTitle(),
                teacher.getDepartment().getId(),
                teacher.getDepartment().getName());
    }
}
