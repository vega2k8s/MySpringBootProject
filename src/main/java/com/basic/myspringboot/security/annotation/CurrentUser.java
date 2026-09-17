package com.basic.myspringboot.security.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 로그인한 사용자의 UserInfo 를 컨트롤러 파라미터로 주입받는 애노테이션.
 *
 * @AuthenticationPrincipal(expression = "userInfo") 를 감싼 것이며,
 * 인증되지 않은 요청( anonymousUser )일 때는 null 이 주입된다.
 *
 * @Target     : 애노테이션을 붙일 수 있는 위치 ( 여기서는 메서드 파라미터 )
 * @Retention  : 애노테이션이 유지되는 시점 ( RUNTIME 이라야 스프링이 읽을 수 있다 )
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : userInfo")
public @interface CurrentUser {
}
