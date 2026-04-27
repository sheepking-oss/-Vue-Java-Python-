package com.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contract.entity.ArchiveRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArchiveRecordMapper extends BaseMapper<ArchiveRecord> {

    IPage<ArchiveRecord> selectPageWithDetails(Page<ArchiveRecord> page,
                                                 @Param("contractName") String contractName,
                                                 @Param("contractNo") String contractNo,
                                                 @Param("archiveNo") String archiveNo,
                                                 @Param("status") Integer status);
}
