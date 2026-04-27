package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.entity.ContractAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractAttachmentMapper extends BaseMapper<ContractAttachment> {

    List<ContractAttachment> selectByContractId(@Param("contractId") Long contractId);

    List<ContractAttachment> selectByVersionId(@Param("versionId") Long versionId);
}
