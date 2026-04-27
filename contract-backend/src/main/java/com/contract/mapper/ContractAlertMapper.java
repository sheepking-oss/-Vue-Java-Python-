package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contract.entity.ContractAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ContractAlertMapper extends BaseMapper<ContractAlert> {

    IPage<ContractAlert> selectPageWithDetails(Page<ContractAlert> page,
                                                 @Param("receiverId") Long receiverId,
                                                 @Param("alertType") Integer alertType,
                                                 @Param("status") Integer status);

    List<ContractAlert> selectPendingAlerts(@Param("alertDate") LocalDate alertDate);

    List<ContractAlert> selectUnalertByDate(@Param("alertDate") LocalDate alertDate);
}
