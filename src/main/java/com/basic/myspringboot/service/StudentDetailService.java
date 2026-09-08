//StudentService 클래스
package com.basic.myspringboot.service;

import com.basic.myspringboot.controller.dto.StudentDTO;
import com.basic.myspringboot.entity.Student;
import com.basic.myspringboot.entity.StudentDetail;
import com.basic.myspringboot.exception.BusinessException;
import com.basic.myspringboot.exception.ErrorCode;
import com.basic.myspringboot.repository.StudentDetailRepository;
import com.basic.myspringboot.repository.DepartmentStudentCount;
import com.basic.myspringboot.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentDetailService {

    private final StudentRepository studentRepository;
    private final StudentDetailRepository studentDetailRepository;

    public List<StudentDTO.Response> getAllStudents() {
        //findAll() 대신 Fetch Join 을 사용하여 N+1 문제를 해결한다
        List<Student> students = studentRepository.findAllWithDetails();
        //학과별 학생 수는 학과마다 COUNT 를 날리지 않도록 한 번에 집계한다
        Map<Long, Long> countByDepartmentId = studentCountByDepartmentId();

        return students.stream()
                .map(student -> StudentDTO.Response.fromEntity(
                        student, countOf(countByDepartmentId, student)))
                .toList();
                //.collect(Collectors.toList());
    }

    public StudentDTO.Response getStudentById(Long id) {
        Student student = studentRepository.findByIdWithStudentDetail(id)
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

    @Transactional
    public StudentDTO.Response createStudent(StudentDTO.Request request) {
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

        // Create student 엔티티 생성
        Student student = Student.builder()
                .name(request.getName())  //이름
                .studentNumber(request.getStudentNumber())  //학번
                .build();

        // Create StudentDetail 엔티티 생성
        if (request.getDetailRequest() != null) {
            StudentDetail studentDetail = StudentDetail.builder()
                    .address(request.getDetailRequest().getAddress()) //주소
                    .phoneNumber(request.getDetailRequest().getPhoneNumber()) //전화번호
                    .email(request.getDetailRequest().getEmail()) //이메일
                    .dateOfBirth(request.getDetailRequest().getDateOfBirth()) //생년월일
                    //생성하는 StudentDetail과 연관된 Student 엔티티 객체를 저장
                    .student(student)
                    .build();
            //양방향 연관관계이므로 Student와 연관된 StduentDetail 엔티티 객체를 저장
            student.setStudentDetail(studentDetail);
        }

        // Student와 StudentDetail의 라이프사이클이 동일하므로 Student만 저장합
        Student savedStudent = studentRepository.save(student);
        // Student를 StudentDTO.Response 로 변환
        return StudentDTO.Response.fromEntity(savedStudent, departmentStudentCount(savedStudent));
    }

    @Transactional
    public StudentDTO.Response updateStudent(Long id, StudentDTO.Request request) {
        // Find the student
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Student", "id", id));

        // 저장된 학번과 요청한 학번이 일치하지 않으면
        if (!student.getStudentNumber().equals(request.getStudentNumber()) &&
                //요청한 학번이 중복되는지 체크하기 위해서 해당학번으로 Student 조회
                studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new BusinessException(ErrorCode.STUDENT_NUMBER_DUPLICATE,
                    request.getStudentNumber());
        }

        // Update student basic info
        student.setName(request.getName());
        student.setStudentNumber(request.getStudentNumber());

        // Update student detail if provided
        if (request.getDetailRequest() != null) {
            //Student가 연관된 StudentDetail 객체를 가져오기
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

            // Create new detail if not exists ( 저장된 StudentDetail 정보가 없을 경우 )
            if (studentDetail == null) {
                // 새로운 StudentDetail 객체생성
                studentDetail = new StudentDetail();
                //연관된 Student 객체 저장
                studentDetail.setStudent(student);
                //연관된 StudenDetail 객체 저장
                student.setStudentDetail(studentDetail);
            }

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

    private boolean isEmailChangingAndExists(StudentDetail currentDetail,
                                             StudentDTO.StudentDetailDTO newDetail) {
        //상세정보가 아직 없는 경우 currentDetail 은 null 이다
        String currentEmail = currentDetail == null ? null : currentDetail.getEmail();
        return newDetail.getEmail() != null &&
                !newDetail.getEmail().isEmpty() &&
                (currentEmail == null || !currentEmail.equals(newDetail.getEmail())) &&
                studentDetailRepository.existsByEmail(newDetail.getEmail());
    }

    private boolean isPhoneNumberChangingAndExists(StudentDetail currentDetail,
                                                   StudentDTO.StudentDetailDTO newDetail) {
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

    /**
     * 전체 학과의 학생 수를 한 번의 집계 쿼리로 구해
     * "학과ID -> 학생수" 형태의 Map 으로 만든다.
     */
    private Map<Long, Long> studentCountByDepartmentId() {
        Map<Long, Long> countByDepartmentId = new HashMap<>();
        for (DepartmentStudentCount row : studentRepository.countGroupByDepartmentId()) {
            countByDepartmentId.put(row.getDepartmentId(), row.getStudentCount());
        }
        return countByDepartmentId;
    }

    /** 집계 결과에서 해당 학생의 학과 학생 수를 꺼낸다. */
    private Long countOf(Map<Long, Long> countByDepartmentId, Student student) {
        return student.getDepartment() == null
                ? null
                : countByDepartmentId.getOrDefault(student.getDepartment().getId(), 0L);
    }
}