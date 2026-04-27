package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.entity.ContractVersionApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ContractVersionApprovalMapper extends BaseMapper<ContractVersionApproval> {

    ContractVersionApproval selectByContractIdAndVersion(@Param("contractId") Long contractId, @Param("versionNo") Integer versionNo);

    ContractVersionApproval selectByInstanceId(@Param("instanceId") Long instanceId);

    ContractVersionApproval selectCurrentByContractId(@Param("contractId") Long contractId);

    int updateIsCurrentByContractId(@Param("contractId") Long contractId, @Param("isCurrent") Integer isCurrent);
}
