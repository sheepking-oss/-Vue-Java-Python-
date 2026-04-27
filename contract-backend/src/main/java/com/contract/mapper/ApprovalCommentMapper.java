package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.entity.ApprovalComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalCommentMapper extends BaseMapper<ApprovalComment> {

    List<ApprovalComment> selectByInstanceId(@Param("instanceId") Long instanceId);
}
