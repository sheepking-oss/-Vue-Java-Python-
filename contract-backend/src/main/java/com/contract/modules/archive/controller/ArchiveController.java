package com.contract.modules.archive.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contract.common.result.PageResult;
import com.contract.common.result.Result;
import com.contract.entity.ArchiveRecord;
import com.contract.mapper.ArchiveRecordMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Api(tags = "归档管理")
@RestController
@RequestMapping("/archive")
public class ArchiveController {

    @Autowired
    private ArchiveRecordMapper archiveRecordMapper;

    @ApiOperation("分页查询归档列表")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('archive:list','archive:view')")
    public Result<PageResult<ArchiveRecord>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size,
            @ApiParam("合同名称") @RequestParam(required = false) String contractName,
            @ApiParam("合同编号") @RequestParam(required = false) String contractNo,
            @ApiParam("归档编号") @RequestParam(required = false) String archiveNo,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {

        Page<ArchiveRecord> pageParam = new Page<>(page, size);
        Page<ArchiveRecord> resultPage = archiveRecordMapper.selectPageWithDetails(
                pageParam, contractName, contractNo, archiveNo, status);

        PageResult<ArchiveRecord> result = PageResult.of(
                resultPage.getRecords(),
                resultPage.getTotal(),
                resultPage.getSize(),
                resultPage.getCurrent()
        );
        return Result.success(result);
    }

    @ApiOperation("获取归档详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('archive:list','archive:view')")
    public Result<ArchiveRecord> getById(@PathVariable Long id) {
        ArchiveRecord record = archiveRecordMapper.selectById(id);
        if (record == null) {
            return Result.error("归档记录不存在");
        }
        return Result.success(record);
    }
}
