package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("contract_version_approval")
public class ContractVersionApproval {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long contractId;
    private Integer versionNo;
    private Long instanceId;
    private Integer isCurrent;
    private LocalDateTime createTime;
}
