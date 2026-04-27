package com.contract.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ApprovalSubmitVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "业务ID不能为空")
    private Long businessId;

    private String businessType;

    private Long flowId;

    private String comment;
}
