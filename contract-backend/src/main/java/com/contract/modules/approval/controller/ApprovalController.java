package com.contract.modules.approval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contract.common.result.PageResult;
import com.contract.common.result.Result;
import com.contract.entity.ApprovalComment;
import com.contract.entity.ApprovalInstance;
import com.contract.entity.ApprovalInstanceNode;
import com.contract.modules.approval.service.ApprovalService;
import com.contract.vo.ApprovalActionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "审批管理")
@RestController
@RequestMapping("/approval")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    @ApiOperation("获取待我审批列表")
    @GetMapping("/my-approval")
    @PreAuthorize("hasAuthority('approval:process')")
    public Result<PageResult<ApprovalInstance>> getMyApprovalList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size,
            @ApiParam("业务类型") @RequestParam(required = false) String businessType,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {

        Page<ApprovalInstance> instancePage = approvalService.getMyApprovalList(page, size, businessType, status);
        PageResult<ApprovalInstance> result = PageResult.of(
                instancePage.getRecords(),
                instancePage.getTotal(),
                instancePage.getSize(),
                instancePage.getCurrent()
        );
        return Result.success(result);
    }

    @ApiOperation("获取我发起的审批列表")
    @GetMapping("/my-initiated")
    @PreAuthorize("hasAuthority('approval:initiate')")
    public Result<PageResult<ApprovalInstance>> getMyInitiatedList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size,
            @ApiParam("业务类型") @RequestParam(required = false) String businessType,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {

        Page<ApprovalInstance> instancePage = approvalService.getMyInitiatedList(page, size, businessType, status);
        PageResult<ApprovalInstance> result = PageResult.of(
                instancePage.getRecords(),
                instancePage.getTotal(),
                instancePage.getSize(),
                instancePage.getCurrent()
        );
        return Result.success(result);
    }

    @ApiOperation("获取审批实例详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('approval:process','approval:view')")
    public Result<ApprovalInstance> getById(@PathVariable Long id) {
        ApprovalInstance instance = approvalService.getById(id);
        if (instance == null) {
            return Result.error("审批实例不存在");
        }
        return Result.success(instance);
    }

    @ApiOperation("根据业务ID获取审批实例")
    @GetMapping("/business/{businessType}/{businessId}")
    @PreAuthorize("hasAnyAuthority('approval:process','approval:view')")
    public Result<ApprovalInstance> getByBusinessId(
            @PathVariable String businessType,
            @PathVariable Long businessId) {
        ApprovalInstance instance = approvalService.getByBusinessId(businessId, businessType);
        return Result.success(instance);
    }

    @ApiOperation("处理审批（通过/驳回/退回）")
    @PostMapping("/process")
    @PreAuthorize("hasAuthority('approval:process')")
    public Result<Void> processApproval(@Validated @RequestBody ApprovalActionVO actionVO) {
        approvalService.processApproval(actionVO);
        String message;
        switch (actionVO.getActionType()) {
            case 1:
                message = "审批通过";
                break;
            case 2:
                message = "审批驳回";
                break;
            case 3:
                message = "已退回";
                break;
            default:
                message = "操作成功";
        }
        return Result.success(message);
    }

    @ApiOperation("获取审批节点列表")
    @GetMapping("/{instanceId}/nodes")
    @PreAuthorize("hasAnyAuthority('approval:process','approval:view')")
    public Result<List<ApprovalInstanceNode>> getInstanceNodes(@PathVariable Long instanceId) {
        List<ApprovalInstanceNode> nodes = approvalService.getInstanceNodes(instanceId);
        return Result.success(nodes);
    }

    @ApiOperation("获取审批意见列表")
    @GetMapping("/{instanceId}/comments")
    @PreAuthorize("hasAnyAuthority('approval:process','approval:view')")
    public Result<List<ApprovalComment>> getInstanceComments(@PathVariable Long instanceId) {
        List<ApprovalComment> comments = approvalService.getInstanceComments(instanceId);
        return Result.success(comments);
    }

    @ApiOperation("撤回审批")
    @PostMapping("/{instanceId}/withdraw")
    @PreAuthorize("hasAuthority('approval:withdraw')")
    public Result<Void> withdrawApproval(@PathVariable Long instanceId) {
        approvalService.withdrawApproval(instanceId);
        return Result.success("撤回成功");
    }
}
