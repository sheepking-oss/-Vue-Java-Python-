package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("approval_instance_node")
public class ApprovalInstanceNode {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long instanceId;
    private Long nodeId;
    private Integer contractVersionNo;
    private String nodeName;
    private Integer nodeType;
    private Integer sort;
    private Integer assigneeType;
    private String assigneeValue;
    private Long actualAssignee;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String comment;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_PASSED = 2;
    public static final int STATUS_REJECTED = 3;
    public static final int STATUS_RETURNED = 4;
    public static final int STATUS_SKIPPED = 5;

    @TableField(exist = false)
    private String actualAssigneeName;

    @TableField(exist = false)
    private List<SysUser> possibleAssignees;
}
