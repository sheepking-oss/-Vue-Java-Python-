package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long parentId;
    private String menuName;
    private Integer menuType;
    private String path;
    private String component;
    private String perms;
    private Integer visible;
    private Integer sort;
    private String icon;
    private Integer status;

    @TableField(exist = false)
    private List<SysPermission> children;
}
