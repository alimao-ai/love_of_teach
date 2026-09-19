package com.loveofteach.platform.api;

import com.loveofteach.platform.api.dto.DepartmentRequest;
import com.loveofteach.platform.api.dto.DepartmentResponse;
import com.loveofteach.platform.api.dto.SemesterRequest;
import com.loveofteach.platform.api.dto.SemesterResponse;
import com.loveofteach.platform.api.dto.StatusRequest;
import com.loveofteach.platform.api.dto.TeacherResponse;
import com.loveofteach.platform.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/departments")
    public List<DepartmentResponse> listDepartments(
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        return catalogService.listDepartments(activeOnly);
    }

    @GetMapping("/departments/{id}")
    public DepartmentResponse getDepartment(@PathVariable Long id) {
        return catalogService.getDepartment(id);
    }

    @PostMapping("/departments")
    public DepartmentResponse createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return catalogService.createDepartment(request);
    }

    @PutMapping("/departments/{id}")
    public DepartmentResponse updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        return catalogService.updateDepartment(id, request);
    }

    @PatchMapping("/departments/{id}/status")
    public DepartmentResponse updateDepartmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request
    ) {
        return catalogService.updateDepartmentStatus(id, request.active());
    }

    @GetMapping("/semesters")
    public List<SemesterResponse> listSemesters(
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        return catalogService.listSemesters(activeOnly);
    }

    @GetMapping("/semesters/{id}")
    public SemesterResponse getSemester(@PathVariable Long id) {
        return catalogService.getSemester(id);
    }

    @PostMapping("/semesters")
    public SemesterResponse createSemester(@Valid @RequestBody SemesterRequest request) {
        return catalogService.createSemester(request);
    }

    @PutMapping("/semesters/{id}")
    public SemesterResponse updateSemester(
            @PathVariable Long id,
            @Valid @RequestBody SemesterRequest request
    ) {
        return catalogService.updateSemester(id, request);
    }

    @PatchMapping("/semesters/{id}/status")
    public SemesterResponse updateSemesterStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request
    ) {
        return catalogService.updateSemesterStatus(id, request.active());
    }

    @GetMapping("/teachers")
    public List<TeacherResponse> listTeachers(
            @RequestParam(required = false) Long departmentId
    ) {
        return catalogService.listTeachers(departmentId);
    }
}
