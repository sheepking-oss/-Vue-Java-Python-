package com.contract.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.entity.SysPermission;
import com.contract.entity.SysRole;
import com.contract.entity.SysUser;
import com.contract.mapper.SysPermissionMapper;
import com.contract.mapper.SysRoleMapper;
import com.contract.mapper.SysUserMapper;
import com.contract.modules.system.service.AuthService;
import com.contract.utils.JwtUtils;
import com.contract.utils.SecurityUtils;
import com.contract.vo.LoginResultVO;
import com.contract.vo.LoginVO;
import com.contract.vo.SysPermissionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysPermissionMapper permissionMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginResultVO login(LoginVO loginVO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginVO.getUsername(), loginVO.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, loginVO.getUsername())
        );

        String token = jwtUtils.generateToken(user.getId(), user.getUserName());

        return buildLoginResult(user, token);
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    public LoginResultVO getCurrentUserInfo() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return null;
        }

        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username)
        );

        if (user == null) {
            return null;
        }

        return buildLoginResult(user, null);
    }

    private LoginResultVO buildLoginResult(SysUser user, String token) {
        LoginResultVO result = new LoginResultVO();
        result.setUserId(user.getId());
        result.setUsername(user.getUserName());
        result.setNickName(user.getNickName());
        result.setAvatar(user.getAvatar());
        result.setDeptId(user.getDeptId());

        if (token != null) {
            result.setToken(jwtUtils.getPrefix() + token);
        }

        List<SysRole> roles = roleMapper.selectByUserId(user.getId());
        List<String> roleKeys = roles.stream()
                .map(SysRole::getRoleKey)
                .collect(Collectors.toList());
        result.setRoles(roleKeys);

        Set<String> permissions = new HashSet<>();
        for (SysRole role : roles) {
            List<SysPermission> rolePermissions = permissionMapper.selectByRoleId(role.getId());
            for (SysPermission permission : rolePermissions) {
                if (permission.getPerms() != null && !permission.getPerms().isEmpty()) {
                    permissions.add(permission.getPerms());
                }
            }
        }
        result.setPermissions(new ArrayList<>(permissions));

        List<SysPermission> userMenus = permissionMapper.selectByUserId(user.getId());
        List<SysPermissionVO> menuTree = buildMenuTree(userMenus);
        result.setMenus(menuTree);

        return result;
    }

    private List<SysPermissionVO> buildMenuTree(List<SysPermission> menus) {
        Map<Long, List<SysPermissionVO>> menuMap = new HashMap<>();
        List<SysPermissionVO> rootMenus = new ArrayList<>();

        for (SysPermission menu : menus) {
            SysPermissionVO vo = new SysPermissionVO();
            BeanUtils.copyProperties(menu, vo);
            vo.setChildren(new ArrayList<>());

            if (menu.getParentId() == 0) {
                rootMenus.add(vo);
            } else {
                menuMap.computeIfAbsent(menu.getParentId(), k -> new ArrayList<>()).add(vo);
            }
        }

        for (SysPermissionVO menu : rootMenus) {
            addChildren(menu, menuMap);
        }

        rootMenus.sort(Comparator.comparingInt(SysPermissionVO::getSort));
        return rootMenus;
    }

    private void addChildren(SysPermissionVO menu, Map<Long, List<SysPermissionVO>> menuMap) {
        List<SysPermissionVO> children = menuMap.get(menu.getId());
        if (children != null && !children.isEmpty()) {
            children.sort(Comparator.comparingInt(SysPermissionVO::getSort));
            menu.setChildren(children);
            for (SysPermissionVO child : children) {
                addChildren(child, menuMap);
            }
        }
    }
}
