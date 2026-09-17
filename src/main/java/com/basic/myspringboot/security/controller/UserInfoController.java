package com.basic.myspringboot.security.controller;

import com.basic.myspringboot.security.controller.dto.AuthRequest;
import com.basic.myspringboot.security.controller.dto.AuthResponse;
import com.basic.myspringboot.security.controller.dto.SignUpRequest;
import com.basic.myspringboot.security.jwt.JwtService;
import com.basic.myspringboot.security.models.UserInfo;
import com.basic.myspringboot.security.models.UserInfoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/userinfos")
@RequiredArgsConstructor
public class UserInfoController {

    /** 회원가입으로 만들어지는 계정의 기본 권한 ( 관리자는 요청으로 받지 않는다 ) */
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserInfoRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    /**
     * 회원가입. 권한은 서버가 ROLE_USER 로 고정한다.
     */
    @PostMapping("/new")
    public ResponseEntity<String> addNewUser(@Valid @RequestBody SignUpRequest request) {
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("이미 사용 중인 이메일입니다 : " + request.getEmail());
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setName(request.getName());
        userInfo.setEmail(request.getEmail());
        //비밀번호는 반드시 암호화해서 저장한다
        userInfo.setPassword(passwordEncoder.encode(request.getPassword()));
        userInfo.setRoles(DEFAULT_ROLE);

        UserInfo savedUserInfo = repository.save(userInfo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedUserInfo.getName() + " user added!!");
    }

    /**
     * 로그인. 인증에 실패하면 AuthenticationException 이 발생하고
     * DefaultExceptionAdvice 가 401 로 응답한다.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateAndGetToken(@Valid @RequestBody AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                ));

        String token = jwtService.generateToken(authRequest.getEmail());
        return ResponseEntity.ok(
                new AuthResponse(token, "Bearer", jwtService.getAccessExpireSeconds()));
    }
}
