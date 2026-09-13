package com.basic.myspringboot.repository;

import com.basic.myspringboot.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * [ 실험 전용 ] 문서 "1:N 컬렉션을 Fetch Join 하면 무슨 일이 생기는가" 를 직접 확인하기 위한 Repository.
 *
 * 테스트 소스에만 있으므로 실제 애플리케이션에는 영향이 없다.
 */
public interface ExpDepartmentRepository extends JpaRepository<Department, Long> {

    /** A. 컬렉션 Fetch Join + 페이징  -> 메모리 페이징 ( HHH90003004 ) */
    @Query(value = "SELECT d FROM Department d LEFT JOIN FETCH d.students",
            countQuery = "SELECT COUNT(d) FROM Department d")
    Page<Department> findAllFetchJoinPaged(Pageable pageable);

    /** B. FETCH 없는 일반 JOIN + 페이징  -> 결과 건수가 틀어진다 */
    @Query(value = "SELECT d FROM Department d LEFT JOIN d.students",
            countQuery = "SELECT COUNT(d) FROM Department d")
    Page<Department> findAllPlainJoinPaged(Pageable pageable);

    /** C. 조인 없이 페이징  -> 정상이지만 students 를 건드리면 N+1 */
    @Query(value = "SELECT d FROM Department d",
            countQuery = "SELECT COUNT(d) FROM Department d")
    Page<Department> findAllNoJoinPaged(Pageable pageable);
}
