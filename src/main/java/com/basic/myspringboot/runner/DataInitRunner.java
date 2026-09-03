package com.basic.myspringboot.runner;

import com.basic.myspringboot.entity.Student;
import com.basic.myspringboot.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        Student student1 = createStudent("Alice Johnson", "CS001");
        Student student2 = createStudent("Bob Smith", "CS002");

        // Electrical Engineering students
        Student student3 = createStudent("Charlie Brown", "EE001");
        Student student4 = createStudent("Diana Wilson", "EE002");

        // Mechanical Engineering students
        Student student5 = createStudent("Edward Davis", "ME001");

        // Business Administration students
        Student student6 = createStudent("Fiona Garcia", "BA001");
        Student student7 = createStudent("George Martinez", "BA002");

        // Computer Science students
        Student student8 = createStudent("Helen Lee", "CS003");

        List<Student> students = studentRepository.saveAll(
                List.of(student1, student2, student3, student4, student5, student6, student7, student8)
        );

        log.info("Created {} students", students.size());
    }

    /**
     * Student 엔티티 객체를 생성한다.
     *
     * @param name          학생 이름
     * @param studentNumber 학번
     * @return 생성된 Student 엔티티
     */
    private Student createStudent(String name, String studentNumber) {
        return Student.builder()
                .name(name)
                .studentNumber(studentNumber)
                .build();
    }
}
