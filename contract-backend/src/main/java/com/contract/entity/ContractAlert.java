package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("contract_alert")
public class ContractAlert {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long contractId;
    private Integer alertType;
    private LocalDate alertDate;
    private Integer alertDays;
    private String alertContent;
    private Long receiverId;
    private String receiverName;
    private Integer status;
    private LocalDateTime alertTime;
    private LocalDateTime handleTime;
    private String handleRemark;
    private LocalDateTime createTime;

    public static final int TYPE_EXPIRE_SOON = 1;
    public static final int TYPE_EXPIRED = 2;
    public static final int TYPE_PAYMENT = 3;
    public static final int TYPE_OTHER = 4;

    public static final int STATUS_UNALERT = 0;
    public static final int STATUS_ALERTED = 1;
    public static final int STATUS_HANDLED = 2;
    public static final int STATUS_IGNORED = 3;
}
