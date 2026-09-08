package com.basic.myspringboot.repository;

import com.basic.myspringboot.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Optional<Department> findByCode(String code);
    
    //학생의 studentDetail 까지 함께 가져온다.
    //Student.studentDetail 은 mappedBy 쪽 @OneToOne 이라 LAZY 가 동작하지 않고,
    //학생 수만큼 상세정보 조회 쿼리가 추가로 발생하므로 여기서 함께 조회한다.
    @Query("SELECT d FROM Department d "
            + "LEFT JOIN FETCH d.students s "
            + "LEFT JOIN FETCH s.studentDetail "
            + "WHERE d.id = :id")
    Optional<Department> findByIdWithStudents(@Param("id") Long id);

    //학과코드로 조회할 때에도 소속 학생을 함께 가져온다 ( 지연로딩 추가 조회를 없앤다 )
    @Query("SELECT d FROM Department d "
            + "LEFT JOIN FETCH d.students s "
            + "LEFT JOIN FETCH s.studentDetail "
            + "WHERE d.code = :code")
    Optional<Department> findByCodeWithStudents(@Param("code") String code);
    
    boolean existsByCode(String code);
    
    boolean existsByName(String name);
}