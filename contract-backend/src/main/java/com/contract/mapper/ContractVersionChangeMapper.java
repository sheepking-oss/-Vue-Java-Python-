package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.entity.ContractVersionChange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractVersionChangeMapper extends BaseMapper<ContractVersionChange> {

    List<ContractVersionChange> selectByContractId(@Param("contractId") Long contractId);

    ContractVersionChange selectLatestByContractId(@Param("contractId") Long contractId);
}
