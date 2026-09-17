package com.basic.myspringboot.security.config;

import com.basic.myspringboot.security.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Spring Security 설정.
 *
 *   - 세션을 쓰지 않는 STATELESS + JWT 구조이므로 폼 로그인을 켜지 않는다.
 *     ( 폼 로그인을 켜두면 인증 실패 시 401 대신 /login 으로 302 리다이렉트된다 )
 *   - 인증 실패는 401, 권한 부족은 403 을 JSON 으로 응답한다.
 *   - UserDetailsService( @Service ) 와 PasswordEncoder 빈이 있으면
 *     Spring Boot 가 DaoAuthenticationProvider 를 자동으로 구성한다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //JWT 를 쓰는 REST API 이므로 CSRF 토큰이 필요 없다
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        //로그인과 회원가입은 토큰 없이 호출할 수 있어야 한다
                        .requestMatchers("/api/userinfos/welcome",
                                "/api/userinfos/new",
                                "/api/userinfos/login").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /** 토큰이 없거나 잘못된 경우 : 401 */
    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다 ( 유효한 토큰이 없습니다 )");
    }

    /** 로그인은 했지만 권한이 부족한 경우 : 403 */
    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다");
    }

    private void writeError(HttpServletResponse response, int status, String message)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of(
                "statusCode", status,
                "message", message,
                "timestamp", LocalDateTime.now().toString()));
    }
}
