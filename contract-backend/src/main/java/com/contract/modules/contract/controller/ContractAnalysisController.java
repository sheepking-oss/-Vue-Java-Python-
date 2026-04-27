package com.contract.modules.contract.controller;

import com.contract.common.result.Result;
import com.contract.entity.ContractAnalysisTask;
import com.contract.modules.contract.service.ContractAnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "合同智能分析")
@RestController
@RequestMapping("/contract/analysis")
public class ContractAnalysisController {

    @Autowired
    private ContractAnalysisService analysisService;

    @ApiOperation("关键信息抽取")
    @PostMapping("/extract")
    @PreAuthorize("hasAnyAuthority('contract:analysis','contract:view')")
    public Result<Map<String, Object>> extractKeyInfo(
            @ApiParam("合同ID") @RequestParam Long contractId,
            @ApiParam("版本号") @RequestParam(required = false) Integer versionNo,
            @RequestBody Map<String, String> body) {
        
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("分析内容不能为空");
        }

        Map<String, Object> result = analysisService.analyzeKeyInfo(contractId, versionNo, content);
        return Result.success(result);
    }

    @ApiOperation("风险字段检测")
    @PostMapping("/risk")
    @PreAuthorize("hasAnyAuthority('contract:analysis','contract:view')")
    public Result<Map<String, Object>> checkRisk(
            @ApiParam("合同ID") @RequestParam Long contractId,
            @ApiParam("版本号") @RequestParam(required = false) Integer versionNo,
            @RequestBody Map<String, String> body) {
        
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("分析内容不能为空");
        }

        Map<String, Object> result = analysisService.analyzeRisk(contractId, versionNo, content);
        return Result.success(result);
    }

    @ApiOperation("完整分析（关键信息+风险检测）")
    @PostMapping("/full")
    @PreAuthorize("hasAnyAuthority('contract:analysis','contract:view')")
    public Result<Map<String, Object>> fullAnalysis(
            @ApiParam("合同ID") @RequestParam Long contractId,
            @ApiParam("版本号") @RequestParam(required = false) Integer versionNo,
            @RequestBody Map<String, String> body) {
        
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("分析内容不能为空");
        }

        Map<String, Object> result = analysisService.analyzeFull(contractId, versionNo, content);
        return Result.success(result);
    }

    @ApiOperation("获取分析任务列表")
    @GetMapping("/tasks/{contractId}")
    @PreAuthorize("hasAnyAuthority('contract:analysis','contract:view')")
    public Result<List<ContractAnalysisTask>> getTasks(
            @ApiParam("合同ID") @PathVariable Long contractId) {
        
        List<ContractAnalysisTask> tasks = analysisService.getTasksByContractId(contractId);
        return Result.success(tasks);
    }

    @ApiOperation("获取最新分析任务")
    @GetMapping("/tasks/latest/{contractId}")
    @PreAuthorize("hasAnyAuthority('contract:analysis','contract:view')")
    public Result<ContractAnalysisTask> getLatestTask(
            @ApiParam("合同ID") @PathVariable Long contractId) {
        
        ContractAnalysisTask task = analysisService.getLatestTask(contractId);
        return Result.success(task);
    }

    @ApiOperation("校验任务绑定关系")
    @GetMapping("/validate")
    @PreAuthorize("hasAnyAuthority('contract:analysis','contract:view')")
    public Result<Map<String, Object>> validateTask(
            @ApiParam("任务ID") @RequestParam String taskId,
            @ApiParam("合同ID") @RequestParam Long contractId) {
        
        boolean valid = analysisService.validateTaskBinding(taskId, contractId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);
        result.put("taskId", taskId);
        result.put("contractId", contractId);
        
        return Result.success(result);
    }

    @ApiOperation("生成任务标识（测试用）")
    @PostMapping("/generate-task-id")
    @PreAuthorize("hasAnyAuthority('contract:analysis')")
    public Result<Map<String, Object>> generateTaskId(
            @ApiParam("合同ID") @RequestParam Long contractId,
            @ApiParam("版本号") @RequestParam(required = false) Integer versionNo) {
        
        String taskId = analysisService.generateTaskId(contractId, versionNo);
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("contractId", contractId);
        result.put("versionNo", versionNo);
        
        return Result.success(result);
    }
}
