package com.basic.myspringboot.service;

import com.basic.myspringboot.controller.dto.DepartmentDTO;
import com.basic.myspringboot.entity.Department;
import com.basic.myspringboot.exception.BusinessException;
import com.basic.myspringboot.exception.ErrorCode;
import com.basic.myspringboot.repository.DepartmentRepository;
import com.basic.myspringboot.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;

    public List<DepartmentDTO.SimpleResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentDTO.SimpleResponse::fromEntity)
                .toList();
    }

    public DepartmentDTO.Response getDepartmentById(Long id) {
        Department department = departmentRepository.findByIdWithStudents(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Department", "id", id));
        return DepartmentDTO.Response.fromEntity(department);
    }

    public DepartmentDTO.Response getDepartmentByCode(String code) {
        Department department = departmentRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Department", "code", code));
        return DepartmentDTO.Response.fromEntity(department);
    }

    @Transactional
    public DepartmentDTO.Response createDepartment(DepartmentDTO.Request request) {
        // Validate department code is not already in use
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new BusinessException(ErrorCode.DEPARTMENT_CODE_DUPLICATE,
                    request.getCode());
        }

        // Validate department name is not already in use
        if (departmentRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.DEPARTMENT_NAME_DUPLICATE,
                    request.getName());
        }

        // Create department entity
        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .build();

        // Save and return the department
        Department savedDepartment = departmentRepository.save(department);
        return DepartmentDTO.Response.fromEntity(savedDepartment);
    }

    @Transactional
    public DepartmentDTO.Response updateDepartment(Long id, DepartmentDTO.Request request) {
        // Find the department
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Department", "id", id));

        // Check if another department already has the code
        if (!department.getCode().equals(request.getCode()) &&
                departmentRepository.existsByCode(request.getCode())) {
            throw new BusinessException(ErrorCode.DEPARTMENT_CODE_DUPLICATE,
                    request.getCode());
        }

        // Check if another department already has the name
        if (!department.getName().equals(request.getName()) &&
                departmentRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.DEPARTMENT_NAME_DUPLICATE,
                    request.getName());
        }

        // Update department info
        department.setName(request.getName());
        department.setCode(request.getCode());

        // Save and return updated department
        Department updatedDepartment = departmentRepository.save(department);
        return DepartmentDTO.Response.fromEntity(updatedDepartment);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Department", "id", id);
        }

        // Check if department has students
        Long studentCount = studentRepository.countByDepartmentId(id);
        if (studentCount > 0) {
            throw new BusinessException(ErrorCode.DEPARTMENT_HAS_STUDENTS,
                    id, studentCount);
        }

        departmentRepository.deleteById(id);
    }
}