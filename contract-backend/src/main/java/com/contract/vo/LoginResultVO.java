package com.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LoginResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String nickName;
    private String avatar;
    private Long deptId;
    private String deptName;
    private String token;
    private List<String> roles;
    private List<String> permissions;
    private List<SysPermissionVO> menus;
}
