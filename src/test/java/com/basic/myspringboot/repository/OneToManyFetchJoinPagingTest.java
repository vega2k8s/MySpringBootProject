package com.basic.myspringboot.repository;

import com.basic.myspringboot.entity.Department;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.function.Supplier;

/**
 * 문서 "1:N 컬렉션을 Fetch Join 하면 무슨 일이 생기는가" 확인용 실험.
 *
 * 데이터 : 학과 3개 ( CS 학생 3명 / EE 학생 2명 / MATH 학생 0명 )
 * 요청   : "학과를 2개만" ( page=0, size=2 )
 *
 * 콘솔에서 볼 것
 *   1) 목록 SQL 에 limit / fetch first 가 붙었는가
 *   2) WARN HHH90003004 경고가 떴는가
 *   3) 돌려받은 학과가 몇 개인가 ( 2개를 요청했다 )
 */
@DataJpaTest
@ActiveProfiles("test")
class OneToManyFetchJoinPagingTest {

    @PersistenceContext
    EntityManager em;
    @Autowired
    EntityManagerFactory emf;
    @Autowired
    ExpDepartmentRepository repository;

    private Statistics stats;

    private void seed() {
        Object[][] depts = {{"컴퓨터공학과", "CS"}, {"전자공학과", "EE"}, {"수학과", "MATH"}};
        for (Object[] d : depts) {
            em.createNativeQuery("INSERT INTO departments (name, code) VALUES (?, ?)")
              .setParameter(1, d[0]).setParameter(2, d[1]).executeUpdate();
        }
        int n = 1;
        int[] counts = {3, 2, 0};
        for (int i = 0; i < counts.length; i++) {
            for (int k = 0; k < counts[i]; k++, n++) {
                em.createNativeQuery(
                        "INSERT INTO students (name, student_number, department_id) VALUES (?, ?, ?)")
                  .setParameter(1, "학생" + n).setParameter(2, "S00" + n)
                  .setParameter(3, i + 1L).executeUpdate();
            }
        }
        em.flush();
        em.clear();
        stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
    }

    private void run(String label, Supplier<Page<Department>> action) {
        em.clear();
        long before = stats.getPrepareStatementCount();
        System.out.println("\n=================================================");
        System.out.println("  " + label);
        System.out.println("=================================================");
        Page<Department> page = action.get();

        StringBuilder sb = new StringBuilder();
        for (Department d : page.getContent()) {
            sb.append("      - ").append(d.getCode())
              .append(" : 학생 ").append(d.getStudents().size()).append("명\n");
        }
        System.out.println("  >> 돌려받은 학과 수 : " + page.getNumberOfElements() + " 개   ( 2개를 요청했다 )");
        System.out.print(sb);
        System.out.println("  >> totalElements    : " + page.getTotalElements());
        System.out.println("  >> 실행된 SQL 수     : "
                + (stats.getPrepareStatementCount() - before) + " 번");
    }

    @Test
    @DisplayName("1:N 컬렉션 Fetch Join 과 페이징 - 세 가지 방식 비교")
    void compare() {
        seed();
        PageRequest twoRows = PageRequest.of(0, 2, Sort.by("id").ascending());

        run("A. 컬렉션 Fetch Join + 페이징   ( LEFT JOIN FETCH d.students )",
            () -> repository.findAllFetchJoinPaged(twoRows));

        run("B. FETCH 없는 일반 JOIN + 페이징 ( LEFT JOIN d.students )",
            () -> repository.findAllPlainJoinPaged(twoRows));

        run("C. 조인 없이 페이징 후 students 접근",
            () -> repository.findAllNoJoinPaged(twoRows));

        System.out.println("\n=================================================");
        System.out.println("  D. 이 예제가 택한 방법 : 프로젝션으로 학생 수만 집계");
        System.out.println("=================================================");
        em.clear();
        long before = stats.getPrepareStatementCount();
        var summaries = em.createQuery(
                "SELECT d.id AS id, d.code AS code, COUNT(s) AS studentCount "
                        + "FROM Department d LEFT JOIN d.students s "
                        + "GROUP BY d.id, d.code", Object[].class)
                .setMaxResults(2).getResultList();
        System.out.println("  >> 돌려받은 학과 수 : " + summaries.size() + " 개");
        for (Object[] row : summaries) {
            System.out.println("      - " + row[1] + " : 학생 " + row[2] + "명");
        }
        System.out.println("  >> 실행된 SQL 수     : "
                + (stats.getPrepareStatementCount() - before) + " 번");
        System.out.println("\n===== 끝 =====\n");
    }
}
