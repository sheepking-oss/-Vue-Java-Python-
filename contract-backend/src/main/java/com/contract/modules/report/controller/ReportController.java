package com.contract.modules.report.controller;

import com.contract.common.result.Result;
import com.contract.mapper.ContractMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Api(tags = "统计报表")
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ContractMapper contractMapper;

    @ApiOperation("按合同类型统计")
    @GetMapping("/contract/by-type")
    @PreAuthorize("hasAuthority('report:view')")
    public Result<List<Map<String, Object>>> getContractStatsByType() {
        List<Map<String, Object>> stats = contractMapper.selectContractStatsByType();
        return Result.success(stats);
    }

    @ApiOperation("按合同状态统计")
    @GetMapping("/contract/by-status")
    @PreAuthorize("hasAuthority('report:view')")
    public Result<List<Map<String, Object>>> getContractStatsByStatus() {
        List<Map<String, Object>> stats = contractMapper.selectContractStatsByStatus();
        return Result.success(stats);
    }

    @ApiOperation("按月度统计合同")
    @GetMapping("/contract/by-month")
    @PreAuthorize("hasAuthority('report:view')")
    public Result<List<Map<String, Object>>> getContractStatsByMonth(
            @ApiParam("开始日期") @RequestParam(required = false) String startDate,
            @ApiParam("结束日期") @RequestParam(required = false) String endDate) {

        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(6);
        LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : LocalDate.now();

        List<Map<String, Object>> stats = contractMapper.selectContractStatsByMonth(start, end);
        return Result.success(stats);
    }

    @ApiOperation("按部门统计合同")
    @GetMapping("/contract/by-dept")
    @PreAuthorize("hasAuthority('report:view')")
    public Result<List<Map<String, Object>>> getContractStatsByDept() {
        List<Map<String, Object>> stats = contractMapper.selectContractStatsByDept();
        return Result.success(stats);
    }
}
