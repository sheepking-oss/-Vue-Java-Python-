package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("contract_attachment")
public class ContractAttachment {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long contractId;
    private Long versionId;
    private Integer versionNo;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String description;
    private Long createBy;
    private LocalDateTime createTime;
    private Integer delFlag;

    @TableField(exist = false)
    private String fileSizeDisplay;

    @TableField(exist = false)
    private String createByName;
}
