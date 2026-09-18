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

    // 페이징 처리 없는 학과 목록 조회
    @GetMapping
    public ResponseEntity<List<DepartmentDTO.SimpleResponse>> getAllDepartments() {
        List<DepartmentDTO.SimpleResponse> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    //--------------------------------------------------------------------
    // [ 수업 비교용 ] 1:N 컬렉션 + 페이징을 잘못 조회하면 어떻게 되는지 직접 호출해 보는 엔드포인트.
    // 콘솔의 SQL 로그와 함께 확인한다. 실제 화면에서는 /api/departments/paged 를 사용한다.
    //--------------------------------------------------------------------

    //(A) 컬렉션 Fetch Join + 페이징 -> WARN HHH90003004, SQL 에 limit 이 없다
    @GetMapping("/demo/fetch-join-paged")
    public ResponseEntity<Page<DepartmentDTO.Response>> getDepartmentsFetchJoinPaged(
            @PageableDefault(size = 2, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(departmentService.getAllDepartmentsWithStudentsPaged(pageable));
    }

    //(B) FETCH 없는 일반 JOIN + 페이징 -> limit 은 붙지만 학과 개수가 틀어진다
    @GetMapping("/demo/plain-join-paged")
    public ResponseEntity<Page<DepartmentDTO.Response>> getDepartmentsPlainJoinPaged(
            @PageableDefault(size = 2, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(departmentService.getAllDepartmentsJoinPaged(pageable));
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

    // 페이징 처리된 특정학과의 학생 목록 조회
    @GetMapping("/{id}/students/paged")
    public ResponseEntity<Page<StudentDTO.Response>> getStudentsByDepartmentIdPaged(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<StudentDTO.Response> students = studentService.getStudentsByDepartmentId(id, pageable);
        return ResponseEntity.ok(students);
    }

    // 페이징 처리 없는 특정학과의 학생 목록 조회
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