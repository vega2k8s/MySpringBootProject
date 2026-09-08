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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;

//    public List<DepartmentDTO.SimpleResponse> getAllDepartments() {
//        return departmentRepository.findAll()
//                .stream()
//                .map(DepartmentDTO.SimpleResponse::fromEntity)
//                .toList();
//    }

    // 모든 학과 조회 - 학생 정보 제외, 학생 수는 한 번의 집계 쿼리로 구한다
    public List<DepartmentDTO.SimpleResponse> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();

        // 학과마다 COUNT 를 날리면 학과 수만큼 쿼리가 발생하므로( N+1 ),
        // GROUP BY 로 한 번에 집계한 뒤 Map 으로 만들어 사용한다
        Map<Long, Long> countByDepartmentId = studentRepository.countGroupByDepartmentId()
                .stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        return departments.stream()
                .map(department -> DepartmentDTO.SimpleResponse.fromEntity(
                        department,
                        // 학생이 한 명도 없는 학과는 집계 결과에 없으므로 0 으로 처리한다
                        countByDepartmentId.getOrDefault(department.getId(), 0L)))
                .toList();
    }


    public DepartmentDTO.Response getDepartmentById(Long id) {
        Department department = departmentRepository.findByIdWithStudents(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Department", "id", id));
        return DepartmentDTO.Response.fromEntity(department);
    }

    public DepartmentDTO.Response getDepartmentByCode(String code) {
        Department department = departmentRepository.findByCodeWithStudents(code)
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