package com.contract.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "合同名称不能为空")
    private String contractName;

    @NotNull(message = "合同类型不能为空")
    private Long typeId;

    @NotBlank(message = "甲方不能为空")
    private String partyA;

    @NotBlank(message = "乙方不能为空")
    private String partyB;

    @NotNull(message = "合同金额不能为空")
    private BigDecimal contractAmount;

    private String currency;

    @NotNull(message = "合同开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "合同结束日期不能为空")
    private LocalDate endDate;

    private LocalDate signDate;

    private String contractContent;

    private String changeReason;
}
