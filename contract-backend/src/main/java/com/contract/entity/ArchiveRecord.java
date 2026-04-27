package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("archive_record")
public class ArchiveRecord {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long contractId;
    private String contractNo;
    private String contractName;
    private String archiveNo;
    private Integer archiveType;
    private String storageLocation;
    private LocalDateTime archiveTime;
    private Long archiveBy;
    private String archiveByName;
    private String remark;
    private Integer status;
    private Long borrowBy;
    private LocalDateTime borrowTime;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static final int TYPE_ELECTRONIC = 1;
    public static final int TYPE_PAPER = 2;

    public static final int STATUS_ARCHIVED = 1;
    public static final int STATUS_BORROWED = 2;
    public static final int STATUS_RETURNED = 3;
    public static final int STATUS_DESTROYED = 4;
}
