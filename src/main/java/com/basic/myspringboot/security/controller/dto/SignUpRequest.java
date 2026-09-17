package com.basic.myspringboot.security.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 회원가입 요청 DTO.
 *
 * UserInfo 엔티티를 그대로 받으면 요청 본문에 roles 를 넣어 스스로 관리자가 될 수 있다.
 * 권한은 서버가 정하므로 요청에서 받지 않는다.
 */
@Data
public class SignUpRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "Password must be 4 characters or more")
    private String password;
}
