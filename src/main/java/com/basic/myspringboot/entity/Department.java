package com.basic.myspringboot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    private List<Student> students = new ArrayList<>();
    
    // Helper methods
    public void addStudent(Student student) {
        students.add(student);
        student.setDepartment(this);
    }
    
    public void removeStudent(Student student) {
        students.remove(student);
        student.setDepartment(null);
    }
}