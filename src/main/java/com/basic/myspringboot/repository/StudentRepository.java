package com.basic.myspringboot.repository;

import com.basic.myspringboot.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * [ 페이징과 Fetch Join ]
 *
 *   @ManyToOne, @OneToOne 같은 ToOne 연관관계는 Fetch Join 을 해도 결과 행이 늘어나지 않으므로
 *   Pageable 과 함께 사용해도 안전하다. 데이터베이스가 LIMIT / OFFSET 으로 페이징한다.
 *
 *   반면 @OneToMany 컬렉션을 Fetch Join 하면 행이 늘어나기 때문에
 *   Pageable 과 함께 쓰면 Hibernate 가 전체를 읽어 메모리에서 페이징한다. ( 위험 )
 *
 *   @Query 에 Fetch Join 을 쓰면서 페이징할 때는 countQuery 를 직접 지정해야 한다.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    //--------------------------------------------------------------------
    // 목록 조회 : 상세정보 + 학과를 함께 가져와 N+1 을 없앤다
    //--------------------------------------------------------------------

    @Query("SELECT s FROM Student s "
            + "LEFT JOIN FETCH s.studentDetail "
            + "LEFT JOIN FETCH s.department")
    List<Student> findAllWithDetails();

    @Query(value = "SELECT s FROM Student s "
            + "LEFT JOIN FETCH s.studentDetail "
            + "LEFT JOIN FETCH s.department",
            countQuery = "SELECT COUNT(s) FROM Student s")
    Page<Student> findAllWithDetails(Pageable pageable);

    //--------------------------------------------------------------------
    // 단건 조회
    //--------------------------------------------------------------------

    //LEFT JOIN FETCH : 상세정보가 없는 학생도 조회되어야 하므로 외부 조인을 사용한다
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.studentDetail WHERE s.id = :id")
    Optional<Student> findByIdWithStudentDetail(@Param("id") Long id);

    @Query("SELECT s FROM Student s "
            + "LEFT JOIN FETCH s.studentDetail "
            + "LEFT JOIN FETCH s.department "
            + "WHERE s.id = :id")
    Optional<Student> findByIdWithAllDetails(@Param("id") Long id);

    //학번으로 조회할 때에도 상세정보/학과를 함께 가져와 쿼리 1번으로 처리한다
    @Query("SELECT s FROM Student s "
            + "LEFT JOIN FETCH s.studentDetail "
            + "LEFT JOIN FETCH s.department "
            + "WHERE s.studentNumber = :studentNumber")
    Optional<Student> findByStudentNumber(@Param("studentNumber") String studentNumber);

    //--------------------------------------------------------------------
    // 학과별 조회
    //--------------------------------------------------------------------

    @Query("SELECT s FROM Student s "
            + "LEFT JOIN FETCH s.studentDetail "
            + "LEFT JOIN FETCH s.department "
            + "WHERE s.department.id = :departmentId")
    List<Student> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query(value = "SELECT s FROM Student s "
            + "LEFT JOIN FETCH s.studentDetail "
            + "LEFT JOIN FETCH s.department "
            + "WHERE s.department.id = :departmentId",
            countQuery = "SELECT COUNT(s) FROM Student s WHERE s.department.id = :departmentId")
    Page<Student> findByDepartmentId(@Param("departmentId") Long departmentId, Pageable pageable);

    //--------------------------------------------------------------------
    // 검색 ( 페이징 )
    //--------------------------------------------------------------------

    @Query(value = "SELECT s FROM Student s "
            + "LEFT JOIN FETCH s.studentDetail "
            + "LEFT JOIN FETCH s.department "
            + "WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))",
            countQuery = "SELECT COUNT(s) FROM Student s "
                    + "WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Student> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT s FROM Student s "
            + "LEFT JOIN FETCH s.studentDetail "
            + "LEFT JOIN FETCH s.department "
            + "WHERE LOWER(s.studentNumber) LIKE LOWER(CONCAT('%', :studentNumber, '%'))",
            countQuery = "SELECT COUNT(s) FROM Student s "
                    + "WHERE LOWER(s.studentNumber) LIKE LOWER(CONCAT('%', :studentNumber, '%'))")
    Page<Student> findByStudentNumberContainingIgnoreCase(@Param("studentNumber") String studentNumber,
                                                          Pageable pageable);

    @Query(value = "SELECT s FROM Student s "
            + "LEFT JOIN FETCH s.studentDetail "
            + "LEFT JOIN FETCH s.department "
            + "WHERE s.department.id = :departmentId "
            + "AND LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))",
            countQuery = "SELECT COUNT(s) FROM Student s "
                    + "WHERE s.department.id = :departmentId "
                    + "AND LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Student> findByDepartmentIdAndNameContainingIgnoreCase(@Param("departmentId") Long departmentId,
                                                                @Param("name") String name,
                                                                Pageable pageable);

    //--------------------------------------------------------------------
    // 집계 / 존재 확인
    //--------------------------------------------------------------------

    //학과 하나의 학생 수 ( 학과 삭제 전 검사에 사용 )
    @Query("SELECT COUNT(s) FROM Student s WHERE s.department.id = :departmentId")
    Long countByDepartmentId(@Param("departmentId") Long departmentId);

    boolean existsByStudentNumber(String studentNumber);
}
