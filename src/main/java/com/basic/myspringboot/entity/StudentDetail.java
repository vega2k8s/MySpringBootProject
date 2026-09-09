package com.basic.myspringboot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

//StudentDetail 클래스
@Entity
@Table(name = "student_details")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
//Owner(주인) - FK(외래키)를 가진 쪽이 주인임
public class StudentDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_detail_id")
    private Long id;
    
    @Column(nullable = false)
    private String address;
    
    //중복될 수 없는 값이므로 DB 에도 UNIQUE 제약을 준다
    @Column(nullable = false, unique = true)
    private String phoneNumber;
    
    //선택 입력 항목이지만, 입력된 경우 중복될 수 없으므로 UNIQUE 제약을 준다
    //UNIQUE 컬럼에도 null 은 여러 개 들어갈 수 있다 ( SQL 에서 null != null )
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column
    private LocalDate dateOfBirth;

    //1:1 지연로딩
    @OneToOne(fetch = FetchType.LAZY)
    //@JoinColumn은 FK(외래키)에 해당하는 어노테이션
    @JoinColumn(name = "student_id", unique = true)
    private Student student;
}