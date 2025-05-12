package com.basic.myspringboot.auth.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfoUserDetails implements UserDetails {

    private String email;
    private String password;
    private List<GrantedAuthority> authorities;
    private UserInfo userInfo;

    public UserInfoUserDetails(UserInfo userInfo) {
        this.userInfo = userInfo;
        //엔티티의 이메일을 username 변수에 저장
        this.email=userInfo.getEmail();
        //엔티티의 패스워드를 password 변수에 저장
        this.password=userInfo.getPassword();
        this.authorities= Arrays.stream(userInfo.getRoles().split(","))
                //.map(roleName -> new SimpleGrantedAuthority(roleName))
                .map(SimpleGrantedAuthority::new)
                //Stream<SimpleGrantedAuthority> => List<SimpleGrantedAuthority>
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    //AuthenticationManager 가 인증을 처리할 때 사용함
    public String getPassword() {
        return password;
    }

    @Override
    //AuthenticationManager 가 인증을 처리할 때 사용함
    public String getUsername() {
        return email;
    }
    
    public UserInfo getUserInfo() {
        return userInfo;
    }    

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}