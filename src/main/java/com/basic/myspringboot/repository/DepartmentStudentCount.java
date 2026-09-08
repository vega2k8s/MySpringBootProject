package com.basic.myspringboot.repository;

/**
 * 학과별 학생 수 집계 결과를 담는 인터페이스.
 * <p>
 * 학생 목록을 조회할 때 각 학생이 속한 학과의 학생 수를 함께 내려주기 위해,
 * 학과마다 COUNT 를 실행하지 않고 한 번의 집계 쿼리로 구할 때 사용한다.
 */
public interface DepartmentStudentCount {

    /** 학과 ID */
    Long getDepartmentId();

    /** 해당 학과에 속한 학생 수 */
    Long getStudentCount();
}
