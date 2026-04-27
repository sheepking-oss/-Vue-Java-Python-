package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.entity.ApprovalInstanceNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalInstanceNodeMapper extends BaseMapper<ApprovalInstanceNode> {

    List<ApprovalInstanceNode> selectByInstanceId(@Param("instanceId") Long instanceId);

    ApprovalInstanceNode selectByInstanceIdAndSort(@Param("instanceId") Long instanceId, @Param("sort") Integer sort);

    List<ApprovalInstanceNode> selectPendingByAssignee(@Param("instanceId") Long instanceId, @Param("assigneeId") Long assigneeId);
}
