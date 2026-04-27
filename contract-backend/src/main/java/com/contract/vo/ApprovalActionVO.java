package com.contract.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ApprovalActionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "审批实例ID不能为空")
    private Long instanceId;

    private Long nodeId;

    @NotNull(message = "操作类型不能为空")
    private Integer actionType;

    private String comment;

    private Long returnNodeId;
}
