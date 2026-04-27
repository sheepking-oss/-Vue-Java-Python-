package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contract.entity.Contract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ContractMapper extends BaseMapper<Contract> {

    IPage<Contract> selectPageWithDetails(Page<Contract> page,
                                           @Param("contractName") String contractName,
                                           @Param("contractNo") String contractNo,
                                           @Param("typeId") Long typeId,
                                           @Param("status") Integer status,
                                           @Param("createBy") Long createBy,
                                           @Param("deptId") Long deptId);

    Contract selectByIdWithDetails(@Param("id") Long id);

    List<Contract> selectExpiringContracts(@Param("alertDays") Integer alertDays);

    List<Map<String, Object>> selectContractStatsByType();

    List<Map<String, Object>> selectContractStatsByStatus();

    List<Map<String, Object>> selectContractStatsByMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Map<String, Object>> selectContractStatsByDept();
}
