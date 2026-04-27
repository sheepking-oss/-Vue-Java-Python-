package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long deptId;
    private String userName;
    private String nickName;
    private String password;
    private String email;
    private String phone;
    private Integer sex;
    private String avatar;
    private Integer status;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private List<SysRole> roles;

    @TableField(exist = false)
    private List<Long> roleIds;

    @TableField(exist = false)
    private List<String> permissions;
}
