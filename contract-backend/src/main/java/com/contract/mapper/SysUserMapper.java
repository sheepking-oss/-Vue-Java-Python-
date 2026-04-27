package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    List<SysUser> selectByRoleId(@Param("roleId") Long roleId);

    List<SysUser> selectByDeptId(@Param("deptId") Long deptId);

    int deleteUserRoleByUserId(@Param("userId") Long userId);

    int insertUserRole(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
