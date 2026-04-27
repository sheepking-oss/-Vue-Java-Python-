package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("approval_comment")
public class ApprovalComment {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long instanceId;
    private Long nodeId;
    private Integer contractVersionNo;
    private Long approverId;
    private String approverName;
    private Integer actionType;
    private String comment;
    private String attachments;
    private LocalDateTime createTime;

    public static final int ACTION_PASS = 1;
    public static final int ACTION_REJECT = 2;
    public static final int ACTION_RETURN = 3;
    public static final int ACTION_WITHDRAW = 4;
    public static final int ACTION_CC = 5;
}
