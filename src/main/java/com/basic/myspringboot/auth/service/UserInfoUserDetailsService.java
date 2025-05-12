package com.basic.myspringboot.auth.service;

import com.basic.myspringboot.auth.model.UserInfo;
import com.basic.myspringboot.auth.model.UserInfoRepository;
import com.basic.myspringboot.auth.model.UserInfoUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserInfoUserDetailsService implements UserDetailsService {
    @Autowired
    private UserInfoRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //로그인 할때 사용자가 입력 username을 인자로 전달 받는다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserInfo> optionalUserInfo = repository.findByEmail(username);
                //입력한 username와 매칭되는 엔티티가 있으면 해당 엔티티를 UserDetail 객체로 전달한다.
        return optionalUserInfo.map(userInfo -> new UserInfoUserDetails(userInfo))
                //userInfo.map(UserInfoUserDetails::new)
                // 입력한 username와 매칭되는 엔티티가 없다면 인증 오류
                .orElseThrow(() -> new UsernameNotFoundException("user not found " + username));

    }

    public String addUser(UserInfo userInfo) {
        //패스워드 인코딩해서 저장
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        //UserInfo 엔티티에 username와 password를 DB에 저장
        UserInfo savedUserInfo = repository.save(userInfo);
        return savedUserInfo.getName() + " user added!!";
    }
}