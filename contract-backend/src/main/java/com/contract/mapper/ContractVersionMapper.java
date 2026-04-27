package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.entity.ContractVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractVersionMapper extends BaseMapper<ContractVersion> {

    List<ContractVersion> selectByContractId(@Param("contractId") Long contractId);

    ContractVersion selectByContractIdAndVersion(@Param("contractId") Long contractId, @Param("versionNo") Integer versionNo);

    Integer selectMaxVersionByContractId(@Param("contractId") Long contractId);
}
