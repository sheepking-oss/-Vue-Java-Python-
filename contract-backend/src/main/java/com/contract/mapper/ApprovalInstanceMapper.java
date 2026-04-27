package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contract.entity.ApprovalInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalInstanceMapper extends BaseMapper<ApprovalInstance> {

    IPage<ApprovalInstance> selectMyApprovalList(Page<ApprovalInstance> page,
                                                   @Param("userId") Long userId,
                                                   @Param("businessType") String businessType,
                                                   @Param("status") Integer status);

    IPage<ApprovalInstance> selectMyInitiatedList(Page<ApprovalInstance> page,
                                                    @Param("userId") Long userId,
                                                    @Param("businessType") String businessType,
                                                    @Param("status") Integer status);

    ApprovalInstance selectByBusinessId(@Param("businessId") Long businessId, @Param("businessType") String businessType);

    ApprovalInstance selectByIdWithDetails(@Param("id") Long id);

    List<ApprovalInstance> selectPendingByAssignee(@Param("assigneeId") Long assigneeId);
}
