package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.entity.ApprovalNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalNodeMapper extends BaseMapper<ApprovalNode> {

    List<ApprovalNode> selectByFlowId(@Param("flowId") Long flowId);

    ApprovalNode selectByFlowIdAndSort(@Param("flowId") Long flowId, @Param("sort") Integer sort);
}
