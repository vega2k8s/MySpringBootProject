package com.basic.myspringboot.controller.dto;

import com.basic.myspringboot.entity.Department;
import com.basic.myspringboot.repository.DepartmentSummary;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

public class DepartmentDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Department name is required")
        @Size(max = 100, message = "Department name cannot exceed 100 characters")
        private String name;

        @NotBlank(message = "Department code is required")
        @Size(max = 10, message = "Department code cannot exceed 10 characters")
        private String code;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String code;
        private Long studentCount;
        private List<StudentDTO.SimpleResponse> students;

        public static Response fromEntity(Department department) {
            return Response.builder()
                    .id(department.getId())
                    .name(department.getName())
                    .code(department.getCode())
                    .studentCount((long) department.getStudents().size())
                    .students(department.getStudents().stream()
                                    //.map(student -> StudentDTO.SimpleResponse.fromEntity(student))
                            .map(StudentDTO.SimpleResponse::fromEntity)
                            .toList())
                    .build();
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimpleResponse {
        private Long id;
        private String name;
        private String code;
        private Long studentCount;

        /**
         * 학과의 기본 정보만 변환한다.
         * students 컬렉션에 접근하지 않으므로 지연로딩 추가 조회가 발생하지 않는다.
         * 학생 수가 필요하면 {@link #fromEntity(Department, Long)} 를 사용한다.
         */
        public static SimpleResponse fromEntity(Department department) {
            return fromEntity(department, null);
        }

        /**
         * 학과 정보와 학생 수를 함께 조회한 결과를 변환한다.
         *
         * @param summary 학과 정보와 학생 수가 함께 담긴 조회 결과
         */
        public static SimpleResponse fromSummary(DepartmentSummary summary) {
            return SimpleResponse.builder()
                    .id(summary.getId())
                    .name(summary.getName())
                    .code(summary.getCode())
                    .studentCount(summary.getStudentCount())
                    .build();
        }

        /**
         * 학과 정보에 별도로 조회한 학생 수를 담아 변환한다.
         *
         * @param studentCount COUNT 쿼리로 미리 구한 학생 수
         */
        public static SimpleResponse fromEntity(Department department, Long studentCount) {
            return SimpleResponse.builder()
                    .id(department.getId())
                    .name(department.getName())
                    .code(department.getCode())
                    .studentCount(studentCount)
                    .build();
        }
    }
}