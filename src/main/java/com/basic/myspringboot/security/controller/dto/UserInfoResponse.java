package com.basic.myspringboot.security.controller.dto;

import com.basic.myspringboot.security.models.UserInfoUserDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * 로그인한 사용자 정보 응답 DTO. ( GET /api/userinfos/me )
 *
 * 클라이언트가 권한( roles )에 따라 버튼을 보여 주거나 숨길 수 있도록 제공한다.
 * 화면 표시용이며, 실제 권한 검사는 서버가 @PreAuthorize 로 한다.
 */
@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private String email;
    private String name;

    /** 예 : ["ROLE_ADMIN", "ROLE_USER"] */
    private List<String> roles;

    /** 인증된 사용자( UserInfoUserDetails )에서 응답을 만든다 */
    public static UserInfoResponse from(UserInfoUserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new UserInfoResponse(
                userDetails.getUsername(),
                userDetails.getUserInfo().getName(),
                roles);
    }
}
