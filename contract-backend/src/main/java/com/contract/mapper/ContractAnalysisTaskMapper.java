package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.entity.ContractAnalysisTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractAnalysisTaskMapper extends BaseMapper<ContractAnalysisTask> {

    ContractAnalysisTask selectByTaskId(@Param("taskId") String taskId);

    List<ContractAnalysisTask> selectByContractId(@Param("contractId") Long contractId);

    ContractAnalysisTask selectLatestByContractId(@Param("contractId") Long contractId);

    int updateStatusByTaskId(@Param("taskId") String taskId, @Param("status") String status);

    int updateResultByTaskId(@Param("taskId") String taskId, 
                              @Param("status") String status,
                              @Param("result") String result,
                              @Param("endTime") java.time.LocalDateTime endTime);

    int updateErrorByTaskId(@Param("taskId") String taskId,
                             @Param("status") String status,
                             @Param("errorMsg") String errorMsg,
                             @Param("endTime") java.time.LocalDateTime endTime);
}
