package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("contract_version_change")
public class ContractVersionChange {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long contractId;
    private Integer fromVersionNo;
    private Integer toVersionNo;
    private String changeType;
    private String changeReason;
    private String changedFields;
    private Long createBy;
    private LocalDateTime createTime;

    public static final String CHANGE_TYPE_CREATE = "CREATE";
    public static final String CHANGE_TYPE_UPDATE = "UPDATE";
    public static final String CHANGE_TYPE_RETURN = "RETURN";
    public static final String CHANGE_TYPE_RETRY = "RETRY";
}
