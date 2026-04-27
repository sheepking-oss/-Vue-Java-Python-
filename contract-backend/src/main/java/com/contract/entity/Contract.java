package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("contract")
public class Contract extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String contractNo;
    private String contractName;
    private Long typeId;
    private String partyA;
    private String partyB;
    private BigDecimal contractAmount;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate signDate;
    private String contractContent;
    private Integer currentVersion;
    private Integer approvalVersionNo;
    private Integer status;
    private Long deptId;

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_APPROVING = 1;
    public static final int STATUS_PASSED = 2;
    public static final int STATUS_REJECTED = 3;
    public static final int STATUS_RETURNED = 4;
    public static final int STATUS_ARCHIVED = 5;
    public static final int STATUS_EXPIRED = 6;

    @TableField(exist = false)
    private String typeName;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private String createByName;

    @TableField(exist = false)
    private List<ContractVersion> versions;

    @TableField(exist = false)
    private List<ContractAttachment> attachments;

    @TableField(exist = false)
    private ApprovalInstance currentApproval;
}
