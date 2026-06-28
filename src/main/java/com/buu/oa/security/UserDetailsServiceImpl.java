package com.buu.oa.security;

import com.buu.oa.entity.SysUser;
import com.buu.oa.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security UserDetailsService实现
 * 从sys_user表加载用户认证信息及角色
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    public UserDetailsServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new UsernameNotFoundException("账号已被禁用: " + username);
        }
        List<String> roleCodes = sysUserMapper.selectRoleCodesByUserId(user.getId());
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmpId(),
                true,
                roleCodes
        );
    }
}
