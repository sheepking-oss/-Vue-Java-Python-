package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long parentId;
    private String deptName;
    private String deptCode;
    private Integer sort;
    private String leader;
    private String phone;
    private String email;
    private Integer status;

    @TableField(exist = false)
    private List<SysDept> children;
}
