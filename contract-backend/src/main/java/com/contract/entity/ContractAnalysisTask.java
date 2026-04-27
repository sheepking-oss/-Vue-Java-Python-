package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("contract_analysis_task")
public class ContractAnalysisTask {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String taskId;
    private Long contractId;
    private Integer versionNo;
    private String analysisType;
    private String status;
    private String contentHash;
    private String result;
    private String errorMsg;
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long createBy;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    public static final String TYPE_KEY_INFO = "KEY_INFO";
    public static final String TYPE_RISK = "RISK";
    public static final String TYPE_FULL = "FULL";
}
