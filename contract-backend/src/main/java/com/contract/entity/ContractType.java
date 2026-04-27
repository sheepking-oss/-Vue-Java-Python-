package com.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("contract_type")
public class ContractType extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String typeName;
    private String typeCode;
    private Long parentId;
    private String description;
    private Integer sort;
    private Integer status;

    @TableField(exist = false)
    private List<ContractType> children;
}
