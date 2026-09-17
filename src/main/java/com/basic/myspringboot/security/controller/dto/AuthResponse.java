package com.basic.myspringboot.security.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인 응답 DTO.
 *
 * 토큰 문자열만 돌려주면 클라이언트가 만료 시간을 알 수 없고,
 * Authorization 헤더에 어떤 형식으로 넣어야 하는지도 드러나지 않는다.
 */
@Getter
@AllArgsConstructor
public class AuthResponse {

    /** 발급된 JWT 액세스 토큰 */
    private String accessToken;

    /** Authorization 헤더에 사용할 토큰 형식 ( Bearer ) */
    private String tokenType;

    /** 토큰 만료까지 남은 시간 ( 초 ) */
    private long expiresIn;
}
