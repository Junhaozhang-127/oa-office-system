package com.buu.oa.controller;

import com.buu.oa.common.R;
import com.buu.oa.dto.LoginRequest;
import com.buu.oa.entity.SysMenu;
import com.buu.oa.entity.SysUser;
import com.buu.oa.mapper.SysUserMapper;
import com.buu.oa.security.JwtUtil;
import com.buu.oa.security.SecurityUtils;
import com.buu.oa.security.UserDetailsImpl;
import com.buu.oa.service.SysMenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证授权Controller
 * 处理登录、登出、获取当前用户信息、菜单权限
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtUtil jwtUtil;
    private final SysMenuService sysMenuService;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtUtil jwtUtil, SysMenuService sysMenuService,
                          SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.sysMenuService = sysMenuService;
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户登录
     * 校验用户名密码，生成JWT Token，返回用户信息、菜单、权限
     * @param loginRequest 登录请求（username + password）
     * @return Token + 用户信息 + 菜单 + 权限
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 直接从数据库查询用户并验证密码
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            return R.fail(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            return R.fail(401, "账号已被禁用");
        }

        // 使用PasswordEncoder验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("用户 {} 密码验证失败", username);
            return R.fail(401, "用户名或密码错误");
        }

        Long userId = user.getId();
        List<String> roleCodes = sysUserMapper.selectRoleCodesByUserId(userId);

        // 构建Spring Security认证上下文（供后续权限注解使用）
        UserDetailsImpl userDetails = new UserDetailsImpl(userId, username, user.getPassword(), user.getEmpId(), true, roleCodes);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成Token
        String token = jwtUtil.generateToken(userId, username, roleCodes);

        // 获取菜单和权限
        List<SysMenu> menus = sysMenuService.getMenuListByUserId(userId);
        List<String> perms = sysMenuService.getPermsByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", userId);
        result.put("username", username);
        result.put("empId", user.getEmpId());
        result.put("roles", roleCodes);
        result.put("menus", menus);
        result.put("permissions", perms);

        log.info("用户 {} 登录成功，角色: {}", username, roleCodes);
        return R.success(result);
    }

    /**
     * 获取当前登录用户信息
     * @return 用户信息 + 菜单 + 权限
     */
    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "未登录");
        }
        String username = SecurityUtils.getCurrentUsername();
        List<String> roleCodes = SecurityUtils.getCurrentRoleCodes();
        List<SysMenu> menus = sysMenuService.getMenuListByUserId(userId);
        List<String> perms = sysMenuService.getPermsByUserId(userId);

        SysUser user = sysUserMapper.selectById(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", username);
        result.put("empId", user != null ? user.getEmpId() : null);
        result.put("roles", roleCodes);
        result.put("menus", menus);
        result.put("permissions", perms);

        return R.success(result);
    }

    /**
     * 退出登录
     * @return 操作结果
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        SecurityContextHolder.clearContext();
        log.info("用户已退出登录");
        return R.success();
    }

    /**
     * 获取当前用户菜单列表
     * @return 菜单列表（目录+菜单，不含按钮）
     */
    @GetMapping("/menus")
    public R<List<SysMenu>> menus() {
        Long userId = SecurityUtils.getCurrentUserId();
        return R.success(sysMenuService.getMenuListByUserId(userId));
    }

    /**
     * 获取当前用户权限编码列表
     * @return 权限编码（如 employee:list）
     */
    @GetMapping("/permissions")
    public R<List<String>> permissions() {
        Long userId = SecurityUtils.getCurrentUserId();
        return R.success(sysMenuService.getPermsByUserId(userId));
    }
}
