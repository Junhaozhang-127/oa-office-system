package com.buu.oa.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetails实现
 * 封装登录用户信息：用户ID、员工ID、角色权限
 */
@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final Long empId;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long userId, String username, String password, Long empId,
                           boolean enabled, List<String> roleCodes) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.empId = empId;
        this.enabled = enabled;
        this.authorities = roleCodes.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
        return enabled;
    }
}
