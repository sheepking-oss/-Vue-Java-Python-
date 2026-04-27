package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_flow")
public class ApprovalFlow extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String flowName;
    private String flowCode;
    private String flowType;
    private String description;
    private Integer status;

    @TableField(exist = false)
    private List<ApprovalNode> nodes;
}
