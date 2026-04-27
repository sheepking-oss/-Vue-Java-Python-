package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_instance")
public class ApprovalInstance {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long businessId;
    private String businessType;
    private Integer contractVersionNo;
    private Long flowId;
    private String instanceName;
    private Long currentNodeId;
    private String currentNodeName;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long createBy;
    private LocalDateTime createTime;

    public static final int STATUS_CANCELLED = 0;
    public static final int STATUS_APPROVING = 1;
    public static final int STATUS_PASSED = 2;
    public static final int STATUS_REJECTED = 3;
    public static final int STATUS_RETURNED = 4;

    @TableField(exist = false)
    private String createByName;

    @TableField(exist = false)
    private String flowName;

    @TableField(exist = false)
    private List<ApprovalInstanceNode> nodes;

    @TableField(exist = false)
    private List<ApprovalComment> comments;
}
