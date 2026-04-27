package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("contract_version")
public class ContractVersion {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long contractId;
    private Integer versionNo;
    private String contractNo;
    private String contractName;
    private Long typeId;
    private String partyA;
    private String partyB;
    private BigDecimal contractAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate signDate;
    private String contractContent;
    private String changeReason;
    private Long createBy;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String createByName;
}
