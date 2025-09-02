package com.basic.myspringboot.controller;

import com.basic.myspringboot.controller.dto.DepartmentDTO;
import com.basic.myspringboot.controller.dto.StudentDTO;
import com.basic.myspringboot.service.DepartmentService;
import com.basic.myspringboot.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final StudentService studentService;

    // 페이징 처리된 학과 목록 조회
    @GetMapping("/paged")
    public ResponseEntity<Page<DepartmentDTO.SimpleResponse>> getAllDepartmentsPaged(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<DepartmentDTO.SimpleResponse> departments = departmentService.getAllDepartments(pageable);
        return ResponseEntity.ok(departments);
    }

    // 기존 메서드 유지 (페이징 없는 버전)
    @GetMapping
    public ResponseEntity<List<DepartmentDTO.SimpleResponse>> getAllDepartments() {
        List<DepartmentDTO.SimpleResponse> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO.Response> getDepartmentById(@PathVariable Long id) {
        DepartmentDTO.Response department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<DepartmentDTO.Response> getDepartmentByCode(@PathVariable String code) {
        DepartmentDTO.Response department = departmentService.getDepartmentByCode(code);
        return ResponseEntity.ok(department);
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<List<StudentDTO.Response>> getStudentsByDepartmentId(@PathVariable Long id) {
        List<StudentDTO.Response> students = studentService.getStudentsByDepartmentId(id);
        return ResponseEntity.ok(students);
    }

    @PostMapping
    public ResponseEntity<DepartmentDTO.Response> createDepartment(@Valid @RequestBody
                                                                       DepartmentDTO.Request request) {
        DepartmentDTO.Response createdDepartment = departmentService.createDepartment(request);
        return new ResponseEntity<>(createdDepartment, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO.Response> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentDTO.Request request) {
        DepartmentDTO.Response updatedDepartment = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(updatedDepartment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}