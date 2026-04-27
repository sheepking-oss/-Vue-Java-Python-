package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.entity.ApprovalFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApprovalFlowMapper extends BaseMapper<ApprovalFlow> {

    ApprovalFlow selectByFlowCode(@Param("flowCode") String flowCode);

    ApprovalFlow selectByIdWithNodes(@Param("id") Long id);
}
