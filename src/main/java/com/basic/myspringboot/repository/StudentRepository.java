package com.basic.myspringboot.repository;

import com.basic.myspringboot.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//StudentRepository 인터페이스
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    //전체 목록을 상세정보와 함께 조회한다 ( findAll() 의 N+1 문제를 해결 )
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.studentDetail")
    List<Student> findAllWithStudentDetail();

    //LEFT JOIN FETCH : 상세정보가 없는 학생도 조회되어야 하므로 외부 조인을 사용한다
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.studentDetail WHERE s.id = :id")
    Optional<Student> findByIdWithStudentDetail(@Param("id") Long id);

    //학번으로 조회할 때에도 상세정보를 함께 가져와 쿼리 1번으로 처리한다
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.studentDetail WHERE s.studentNumber = :studentNumber")
    Optional<Student> findByStudentNumber(@Param("studentNumber") String studentNumber);

    boolean existsByStudentNumber(String studentNumber);
}
