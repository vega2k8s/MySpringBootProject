package com.basic.myspringboot.controller;

import com.basic.myspringboot.controller.dto.StudentDTO;
import com.basic.myspringboot.security.models.UserInfo;
import com.basic.myspringboot.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // 페이징 처리된 학생 목록 조회
    @GetMapping("/paged")
    public ResponseEntity<Page<StudentDTO.Response>> getAllStudentsPaged(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        Page<StudentDTO.Response> students = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(students);
    }

    // 페이징 처리 없는 학생 목록 조회
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<StudentDTO.Response>> getAllStudents() {
        List<StudentDTO.Response> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<StudentDTO.Response> getStudentById(@PathVariable Long id) {
        StudentDTO.Response student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @GetMapping("/number/{studentNumber}")
    public ResponseEntity<StudentDTO.Response> getStudentByStudentNumber(@PathVariable String studentNumber) {
        StudentDTO.Response student = studentService.getStudentByStudentNumber(studentNumber);
        return ResponseEntity.ok(student);
    }

    @PostMapping
    public ResponseEntity<StudentDTO.Response> createStudent(
            @Valid @RequestBody StudentDTO.Request request,
            @AuthenticationPrincipal(expression = "userInfo") UserInfo currentUser) {
        StudentDTO.Response createdStudent = studentService.createStudent(request, currentUser);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO.Response> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentDTO.Request request) {
        StudentDTO.Response updatedStudent = studentService.updateStudent(id, request);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}