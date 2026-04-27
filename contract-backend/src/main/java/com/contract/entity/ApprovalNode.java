package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_node")
public class ApprovalNode extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long flowId;
    private String nodeName;
    private String nodeCode;
    private Integer nodeType;
    private Integer sort;
    private Integer approveType;
    private Integer assigneeType;
    private String assigneeValue;
    private String conditions;
    private String description;

    public static final int NODE_TYPE_APPROVE = 1;
    public static final int NODE_TYPE_CC = 2;
    public static final int NODE_TYPE_CONDITION = 3;
    public static final int NODE_TYPE_END = 4;

    public static final int APPROVE_TYPE_OR = 1;
    public static final int APPROVE_TYPE_ALL = 2;
    public static final int APPROVE_TYPE_SEQUENTIAL = 3;

    public static final int ASSIGNEE_TYPE_USER = 1;
    public static final int ASSIGNEE_TYPE_ROLE = 2;
    public static final int ASSIGNEE_TYPE_DEPT_LEADER = 3;
    public static final int ASSIGNEE_TYPE_SELECT = 4;
}
