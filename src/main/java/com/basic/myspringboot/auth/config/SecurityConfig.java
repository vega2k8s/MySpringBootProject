package com.basic.myspringboot.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                    auth.requestMatchers("/api/users/welcome").permitAll()
                            // /api/users/** 패스는 인증 후에만 접근 가능한 경로임
                            .requestMatchers("/api/users/**").authenticated();
                })
                // 스프링이 제공한 인증 Form을 사용함
                .formLogin(withDefaults())
                .build();
    }

    @Bean
    //authentication
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.withUsername("adminboot")
                .password(encoder.encode("pwd1"))
                .roles("ADMIN")  //관리자
                .build();

        UserDetails user = User.withUsername("userboot")
                .password(encoder.encode("pwd2"))
                .roles("USER") //사용자
                .build();
        return new InMemoryUserDetailsManager(admin, user);
    }
}
