package com.basic.myspringboot.controller;

import com.basic.myspringboot.controller.dto.StudentDTO;
import com.basic.myspringboot.service.StudentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class StudentSearchController {

    private final StudentSearchService studentSearchService;

    /**
     * 학생 통합 검색 (이름 또는 학번)
     * GET /api/search/students?keyword=Alice&page=0&size=10&sort=name,asc
     */
    @GetMapping("/students")
    public ResponseEntity<Page<StudentDTO.Response>> searchStudents(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        
        Page<StudentDTO.Response> students = studentSearchService.searchStudents(keyword, pageable);
        return ResponseEntity.ok(students);
    }

    /**
     * 부서별 학생 검색
     * GET /api/search/departments/1/students?keyword=Alice&page=0&size=10
     */
    @GetMapping("/departments/{departmentId}/students")
    public ResponseEntity<Page<StudentDTO.Response>> searchStudentsByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        
        Page<StudentDTO.Response> students = studentSearchService.searchStudentsByDepartment(
                departmentId, keyword, pageable);
        return ResponseEntity.ok(students);
    }

    /**
     * 이름으로 학생 검색
     * GET /api/search/students/by-name?name=Alice&page=0&size=10
     */
    @GetMapping("/students/by-name")
    public ResponseEntity<Page<StudentDTO.Response>> searchStudentsByName(
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        
        Page<StudentDTO.Response> students = studentSearchService.searchStudentsByName(name, pageable);
        return ResponseEntity.ok(students);
    }

    /**
     * 학번으로 학생 검색
     * GET /api/search/students/by-number?studentNumber=CS001&page=0&size=10
     */
    @GetMapping("/students/by-number")
    public ResponseEntity<Page<StudentDTO.Response>> searchStudentsByStudentNumber(
            @RequestParam String studentNumber,
            @PageableDefault(size = 10, sort = "studentNumber", direction = Sort.Direction.ASC) Pageable pageable) {
        
        Page<StudentDTO.Response> students = studentSearchService.searchStudentsByStudentNumber(
                studentNumber, pageable);
        return ResponseEntity.ok(students);
    }
}