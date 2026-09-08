package com.basic.myspringboot.service;

import com.basic.myspringboot.controller.dto.StudentDTO;
import com.basic.myspringboot.entity.Department;
import com.basic.myspringboot.entity.Student;
import com.basic.myspringboot.entity.StudentDetail;
import com.basic.myspringboot.exception.BusinessException;
import com.basic.myspringboot.exception.ErrorCode;
import com.basic.myspringboot.repository.DepartmentRepository;
import com.basic.myspringboot.repository.StudentDetailRepository;
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentDetailRepository studentDetailRepository;
    private final DepartmentRepository departmentRepository;

    public List<StudentDTO.Response> getAllStudents() {
        //findAll() 대신 Fetch Join 을 사용하여 N+1 문제를 해결한다
        List<Student> students = studentRepository.findAllWithDetails();
        //학과별 학생 수는 학과마다 COUNT 를 날리지 않도록 한 번에 집계한다
        Map<Long, Long> countByDepartmentId = studentCountByDepartmentId();

        return students.stream()
                .map(student -> StudentDTO.Response.fromEntity(
                        student, countOf(countByDepartmentId, student)))
                .toList();
    }

    public StudentDTO.Response getStudentById(Long id) {
        Student student = studentRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Student", "id", id));
        return StudentDTO.Response.fromEntity(student, departmentStudentCount(student));
    }

    public StudentDTO.Response getStudentByStudentNumber(String studentNumber) {
        Student student = studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Student", "student number", studentNumber));
        return StudentDTO.Response.fromEntity(student, departmentStudentCount(student));
    }

    public List<StudentDTO.Response> getStudentsByDepartmentId(Long departmentId) {
        // Validate department exists
        if (!departmentRepository.existsById(departmentId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Department", "id", departmentId);
        }

        //학과가 하나로 정해져 있으므로 COUNT 쿼리 1번이면 충분하다
        Long studentCount = studentRepository.countByDepartmentId(departmentId);

        return studentRepository.findByDepartmentId(departmentId)
                .stream()
                .map(student -> StudentDTO.Response.fromEntity(student, studentCount))
                .toList();
    }

    @Transactional
    public StudentDTO.Response createStudent(StudentDTO.Request request) {
        // Validate department exists
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Department", "id", request.getDepartmentId()));

        // Validate student number is not already in use
        if (studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new BusinessException(ErrorCode.STUDENT_NUMBER_DUPLICATE,
                    request.getStudentNumber());
        }

        // Validate email is not already in use (if provided)
        if (hasEmailAndExists(request.getDetailRequest())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATE,
                    request.getDetailRequest().getEmail());
        }

        // Validate phone number is not already in use
        if (hasDetailAndPhoneNumberExists(request.getDetailRequest())) {
            throw new BusinessException(ErrorCode.PHONE_NUMBER_DUPLICATE,
                    request.getDetailRequest().getPhoneNumber());
        }

        // Create student entity
        Student student = Student.builder()
                .name(request.getName())
                .studentNumber(request.getStudentNumber())
                //Student 와 Department 연관관계 저장
                .department(department)
                .build();

        // Create student detail if provided
        if (request.getDetailRequest() != null) {
            StudentDetail studentDetail = StudentDetail.builder()
                    .address(request.getDetailRequest().getAddress())
                    .phoneNumber(request.getDetailRequest().getPhoneNumber())
                    .email(request.getDetailRequest().getEmail())
                    .dateOfBirth(request.getDetailRequest().getDateOfBirth())
                    //StudentDetail 과 Student 연관관계 저장
                    .student(student)
                    .build();

            //Student 와 StudentDetail 연관관계 저장
            student.setStudentDetail(studentDetail);
        }

        // Save and return the student
        Student savedStudent = studentRepository.save(student);
        return StudentDTO.Response.fromEntity(savedStudent, departmentStudentCount(savedStudent));
    }

    @Transactional
    public StudentDTO.Response updateStudent(Long id, StudentDTO.Request request) {
        // Find the student
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Student", "id", id));

        // Validate department exists (if changing)
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Department", "id", request.getDepartmentId()));

        // Check if another student already has the student number
        if (!student.getStudentNumber().equals(request.getStudentNumber()) &&
                studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new BusinessException(ErrorCode.STUDENT_NUMBER_DUPLICATE,
                    request.getStudentNumber());
        }

        // Update student basic info
        student.setName(request.getName());
        student.setStudentNumber(request.getStudentNumber());
        student.setDepartment(department);

        // Update student detail if provided
        if (request.getDetailRequest() != null) {
            StudentDetail studentDetail = student.getStudentDetail();

            // 중복 검사를 먼저 수행한다.
            // 검사용 조회 쿼리가 실행되면 영속성 컨텍스트가 flush 되는데,
            // 값이 채워지지 않은 StudentDetail 을 먼저 연결해 두면
            // address 등 NOT NULL 컬럼에 null 이 들어가 제약조건 위반이 발생한다.
            // Validate email is not already in use (if changing)
            if (isEmailChangingAndExists(studentDetail, request.getDetailRequest())) {
                throw new BusinessException(ErrorCode.EMAIL_DUPLICATE,
                        request.getDetailRequest().getEmail());
            }

            // Validate phone number is not already in use (if changing)
            if (isPhoneNumberChangingAndExists(studentDetail, request.getDetailRequest())) {
                throw new BusinessException(ErrorCode.PHONE_NUMBER_DUPLICATE,
                        request.getDetailRequest().getPhoneNumber());
            }

            // Create new detail if not exists
            // StudentDetail 가 없어서 새롭게 StudentDetail 를 저장하는 경우 
            if (studentDetail == null) {
                studentDetail = new StudentDetail();
                //양방향 연관관계 저장
                studentDetail.setStudent(student);
                student.setStudentDetail(studentDetail);
            }

            // StudentDetail 가 있고, StudentDetail 를 수정하는 경우 
            // Update detail fields
            studentDetail.setAddress(request.getDetailRequest().getAddress());
            studentDetail.setPhoneNumber(request.getDetailRequest().getPhoneNumber());
            studentDetail.setEmail(request.getDetailRequest().getEmail());
            studentDetail.setDateOfBirth(request.getDetailRequest().getDateOfBirth());
        }

        // Save and return updated student
        Student updatedStudent = studentRepository.save(student);
        return StudentDTO.Response.fromEntity(updatedStudent, departmentStudentCount(updatedStudent));
    }

    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Student", "id", id);
        }
        studentRepository.deleteById(id);
    }

    // Helper methods to improve readability and reduce duplication

    private boolean hasEmailAndExists(StudentDTO.StudentDetailDTO detailRequest) {
        return detailRequest != null &&
                detailRequest.getEmail() != null &&
                !detailRequest.getEmail().isEmpty() &&
                studentDetailRepository.existsByEmail(detailRequest.getEmail());
    }

    private boolean hasDetailAndPhoneNumberExists(StudentDTO.StudentDetailDTO detailRequest) {
        return detailRequest != null &&
                studentDetailRepository.existsByPhoneNumber(detailRequest.getPhoneNumber());
    }

    private boolean isEmailChangingAndExists(StudentDetail currentDetail, StudentDTO.StudentDetailDTO newDetail) {
        //상세정보가 아직 없는 경우 currentDetail 은 null 이다
        String currentEmail = currentDetail == null ? null : currentDetail.getEmail();
        return newDetail.getEmail() != null &&
                !newDetail.getEmail().isEmpty() &&
                (currentEmail == null || !currentEmail.equals(newDetail.getEmail())) &&
                studentDetailRepository.existsByEmail(newDetail.getEmail());
    }

    private boolean isPhoneNumberChangingAndExists(StudentDetail currentDetail, StudentDTO.StudentDetailDTO newDetail) {
        //상세정보가 아직 없는 경우 currentDetail 은 null 이다
        String currentPhone = currentDetail == null ? null : currentDetail.getPhoneNumber();
        return (currentPhone == null ||
                !currentPhone.equals(newDetail.getPhoneNumber())) &&
                studentDetailRepository.existsByPhoneNumber(newDetail.getPhoneNumber());
    }

    /**
     * 학생이 속한 학과의 학생 수를 COUNT 쿼리로 구한다.
     * 학과의 students 컬렉션을 통째로 로딩하지 않기 위한 것이다.
     */
    private Long departmentStudentCount(Student student) {
        return student.getDepartment() == null
                ? null
                : studentRepository.countByDepartmentId(student.getDepartment().getId());
    }

    /** 전체 학과의 학생 수를 한 번의 집계 쿼리로 구한다. */
    private Map<Long, Long> studentCountByDepartmentId() {
        return studentRepository.countGroupByDepartmentId()
                .stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }

    /** 집계 결과에서 해당 학생의 학과 학생 수를 꺼낸다. */
    private Long countOf(Map<Long, Long> countByDepartmentId, Student student) {
        return student.getDepartment() == null
                ? null
                : countByDepartmentId.getOrDefault(student.getDepartment().getId(), 0L);
    }
}