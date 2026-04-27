package com.contract.modules.contract.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contract.common.result.PageResult;
import com.contract.common.result.Result;
import com.contract.entity.Contract;
import com.contract.entity.ContractAttachment;
import com.contract.entity.ContractVersion;
import com.contract.modules.contract.service.ContractService;
import com.contract.vo.ContractVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Api(tags = "合同管理")
@RestController
@RequestMapping("/contract")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @ApiOperation("分页查询合同列表")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('contract:list','contract:view')")
    public Result<PageResult<Contract>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size,
            @ApiParam("合同名称") @RequestParam(required = false) String contractName,
            @ApiParam("合同编号") @RequestParam(required = false) String contractNo,
            @ApiParam("合同类型ID") @RequestParam(required = false) Long typeId,
            @ApiParam("合同状态") @RequestParam(required = false) Integer status,
            @ApiParam("创建人ID") @RequestParam(required = false) Long createBy,
            @ApiParam("部门ID") @RequestParam(required = false) Long deptId) {

        Page<Contract> contractPage = contractService.getPage(page, size, contractName, contractNo, typeId, status, createBy, deptId);
        PageResult<Contract> result = PageResult.of(
                contractPage.getRecords(),
                contractPage.getTotal(),
                contractPage.getSize(),
                contractPage.getCurrent()
        );
        return Result.success(result);
    }

    @ApiOperation("获取合同详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('contract:list','contract:view')")
    public Result<Contract> getById(@PathVariable Long id) {
        Contract contract = contractService.getById(id);
        if (contract == null) {
            return Result.error("合同不存在");
        }
        return Result.success(contract);
    }

    @ApiOperation("创建合同")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('contract:create')")
    public Result<Contract> create(@Validated @RequestBody ContractVO contractVO) {
        Contract contract = contractService.create(contractVO);
        return Result.success("创建成功", contract);
    }

    @ApiOperation("更新合同")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('contract:update')")
    public Result<Contract> update(@PathVariable Long id, @Validated @RequestBody ContractVO contractVO) {
        Contract contract = contractService.update(id, contractVO);
        return Result.success("更新成功", contract);
    }

    @ApiOperation("删除合同")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('contract:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return Result.success();
    }

    @ApiOperation("提交审批")
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('contract:submit')")
    public Result<Void> submitApproval(@PathVariable Long id) {
        contractService.submitApproval(id);
        return Result.success("提交审批成功");
    }

    @ApiOperation("创建新版本")
    @PostMapping("/{id}/version")
    @PreAuthorize("hasAuthority('contract:update')")
    public Result<ContractVersion> createVersion(
            @PathVariable Long id,
            @ApiParam("变更原因") @RequestParam(required = false) String changeReason) {
        ContractVersion version = contractService.createVersion(id, changeReason);
        return Result.success("版本创建成功", version);
    }

    @ApiOperation("获取合同版本列表")
    @GetMapping("/{id}/versions")
    @PreAuthorize("hasAnyAuthority('contract:list','contract:view')")
    public Result<List<ContractVersion>> getVersions(@PathVariable Long id) {
        List<ContractVersion> versions = contractService.getVersions(id);
        return Result.success(versions);
    }

    @ApiOperation("上传附件")
    @PostMapping("/{id}/attachment")
    @PreAuthorize("hasAuthority('contract:update')")
    public Result<ContractAttachment> uploadAttachment(
            @PathVariable Long id,
            @ApiParam("文件") @RequestParam("file") MultipartFile file,
            @ApiParam("描述") @RequestParam(required = false) String description) {
        ContractAttachment attachment = contractService.uploadAttachment(id, file, description);
        return Result.success("上传成功", attachment);
    }

    @ApiOperation("获取附件列表")
    @GetMapping("/{id}/attachments")
    @PreAuthorize("hasAnyAuthority('contract:list','contract:view')")
    public Result<List<ContractAttachment>> getAttachments(@PathVariable Long id) {
        List<ContractAttachment> attachments = contractService.getAttachments(id);
        return Result.success(attachments);
    }

    @ApiOperation("删除附件")
    @DeleteMapping("/attachment/{attachmentId}")
    @PreAuthorize("hasAuthority('contract:delete')")
    public Result<Void> deleteAttachment(@PathVariable Long attachmentId) {
        contractService.deleteAttachment(attachmentId);
        return Result.success();
    }

    @ApiOperation("归档合同")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('contract:archive')")
    public Result<Void> archive(@PathVariable Long id) {
        contractService.archive(id);
        return Result.success("归档成功");
    }
}
