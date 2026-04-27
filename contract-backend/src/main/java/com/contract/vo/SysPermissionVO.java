package com.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SysPermissionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String menuName;
    private Integer menuType;
    private String path;
    private String component;
    private String perms;
    private Integer visible;
    private Integer sort;
    private String icon;
    private List<SysPermissionVO> children;
}
