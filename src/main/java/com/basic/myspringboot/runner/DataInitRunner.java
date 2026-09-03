package com.basic.myspringboot.runner;

import com.basic.myspringboot.entity.Student;
import com.basic.myspringboot.entity.StudentDetail;
import com.basic.myspringboot.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataInitRunner implements CommandLineRunner {

    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        // Create students
        createStudents();

        log.info("Data initialization completed successfully");
    }

    private void createStudents() {
        log.info("Creating students...");


        // Computer Science students
        Student student1 = createStudentWithDetail(
                "Alice Johnson", "CS001",
                "123 Tech Street", "010-1234-5678", "alice@example.com",
                LocalDate.of(1998, 3, 15)
        );

        Student student2 = createStudentWithDetail(
                "Bob Smith", "CS002",
                "456 Code Avenue", "010-2345-6789", "bob@example.com",
                LocalDate.of(1997, 7, 22)
        );

        // Electrical Engineering students
        Student student3 = createStudentWithDetail(
                "Charlie Brown", "EE001",
                "789 Circuit Lane", "010-3456-7890", "charlie@example.com",
                LocalDate.of(1999, 11, 8)
        );

        Student student4 = createStudentWithDetail(
                "Diana Wilson", "EE002",
                "321 Power Street", "010-4567-8901", "diana@example.com",
                LocalDate.of(1998, 5, 30)
        );

        // Mechanical Engineering students
        Student student5 = createStudentWithDetail(
                "Edward Davis", "ME001",
                "654 Engine Road", "010-5678-9012", "edward@example.com",
                LocalDate.of(1997, 12, 12)
        );

        // Business Administration students
        Student student6 = createStudentWithDetail(
                "Fiona Garcia", "BA001",
                "987 Business Plaza", "010-6789-0123", "fiona@example.com",
                LocalDate.of(1999, 2, 28)
        );

        Student student7 = createStudentWithDetail(
                "George Martinez", "BA002",
                "147 Commerce Street", "010-7890-1234", "george@example.com",
                LocalDate.of(1998, 9, 10)
        );

        // Student without detail (Computer Science)
        Student student8 = Student.builder()
                .name("Helen Lee")
                .studentNumber("CS003")
                .build();

        List<Student> students = studentRepository.saveAll(
                List.of(student1, student2, student3, student4, student5, student6, student7, student8)
        );

        log.info("Created {} students", students.size());
    }

    private Student createStudentWithDetail(String name, String studentNumber,
                                         String address, String phoneNumber, String email, LocalDate dateOfBirth) {
        //StudentDetail
        StudentDetail detail = StudentDetail.builder()
                .address(address)
                .phoneNumber(phoneNumber)
                .email(email)
                .dateOfBirth(dateOfBirth)
                .build();
        //Student
        Student student = Student.builder()
                .name(name)
                .studentNumber(studentNumber)
                //양방향 연관관계 설정
                .studentDetail(detail)
                .build();

        //양방향 연관관계 설정
        detail.setStudent(student);
        return student;
    }
}