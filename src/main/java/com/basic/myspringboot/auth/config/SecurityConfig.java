package com.basic.myspringboot.auth.config;

import com.basic.myspringboot.auth.service.UserInfoUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> {
                    // /api/users/welcome 패스는 인증 없이 접근 가능한 경로
                    // /userinfos/new 패스는 user_info 엔티티를 추가하는 경로는 인증 필요 없음
                    auth.requestMatchers("/api/users/welcome","/userinfos/new").permitAll()
                            // /api/users/** 패스는 인증 후에만 접근 가능한 경로임
                            .requestMatchers("/api/users/**").authenticated();
                })
                // 스프링이 제공한 인증 Form을 사용함
                .formLogin(withDefaults())
                .build();
    }

    // 개발자가 커스텀한 UserServiceDetail 서비스를 SpringBean으로 등록하기
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserInfoUserDetailsService();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        // UserServiceDetail 서비스가 어떤 객체인지 알려주기
        authenticationProvider.setUserDetailsService(userDetailsService());
        // 패스워드 인코더가 어떤 객체인지 알려주기
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

//    @Bean
//    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
//        UserDetails admin = User.withUsername("adminboot")
//                .password(encoder.encode("pwd1"))
//                .roles("ADMIN")  //관리자
//                .build();
//
//        UserDetails user = User.withUsername("userboot")
//                .password(encoder.encode("pwd2"))
//                .roles("USER") //사용자
//                .build();
//        return new InMemoryUserDetailsManager(admin, user);
//    }
}
