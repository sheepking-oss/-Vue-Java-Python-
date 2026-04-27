package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    List<SysPermission> selectByRoleId(@Param("roleId") Long roleId);

    List<SysPermission> selectByUserId(@Param("userId") Long userId);
}
