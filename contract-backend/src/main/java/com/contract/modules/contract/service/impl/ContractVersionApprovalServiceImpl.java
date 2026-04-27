package com.contract.modules.contract.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.common.exception.BusinessException;
import com.contract.entity.*;
import com.contract.mapper.*;
import com.contract.modules.approval.service.ApprovalService;
import com.contract.modules.contract.service.ContractVersionApprovalService;
import com.contract.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContractVersionApprovalServiceImpl implements ContractVersionApprovalService {

    @Autowired
    private ContractMapper contractMapper;

    @Autowired
    private ContractVersionMapper versionMapper;

    @Autowired
    private ContractAttachmentMapper attachmentMapper;

    @Autowired
    private ContractVersionApprovalMapper versionApprovalMapper;

    @Autowired
    private ContractVersionChangeMapper versionChangeMapper;

    @Autowired
    private ApprovalService approvalService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContractVersionApproval bindVersionWithApproval(Long contractId, Integer versionNo, Long instanceId) {
        ContractVersionApproval existing = versionApprovalMapper.selectCurrentByContractId(contractId);
        if (existing != null && existing.getIsCurrent() == 1) {
            throw new BusinessException("该合同已有正在进行的审批流程");
        }

        ContractVersionApproval versionApproval = new ContractVersionApproval();
        versionApproval.setContractId(contractId);
        versionApproval.setVersionNo(versionNo);
        versionApproval.setInstanceId(instanceId);
        versionApproval.setIsCurrent(1);
        versionApproval.setCreateTime(LocalDateTime.now());

        versionApprovalMapper.insert(versionApproval);

        Contract contract = contractMapper.selectById(contractId);
        if (contract != null) {
            contract.setApprovalVersionNo(versionNo);
            contractMapper.updateById(contract);
        }

        return versionApproval;
    }

    @Override
    public ContractVersionApproval getCurrentVersionApproval(Long contractId) {
        return versionApprovalMapper.selectCurrentByContractId(contractId);
    }

    @Override
    public ContractVersionApproval getVersionApproval(Long contractId, Integer versionNo) {
        return versionApprovalMapper.selectByContractIdAndVersion(contractId, versionNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContractVersion createNewVersionFromReturn(Long contractId, String changeReason) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        Integer currentVersion = contract.getCurrentVersion();
        if (currentVersion == null) {
            currentVersion = 1;
        }

        ContractVersion newVersion = new ContractVersion();
        newVersion.setContractId(contractId);
        newVersion.setVersionNo(currentVersion + 1);
        newVersion.setContractNo(contract.getContractNo());
        newVersion.setContractName(contract.getContractName());
        newVersion.setTypeId(contract.getTypeId());
        newVersion.setPartyA(contract.getPartyA());
        newVersion.setPartyB(contract.getPartyB());
        newVersion.setContractAmount(contract.getContractAmount());
        newVersion.setStartDate(contract.getStartDate());
        newVersion.setEndDate(contract.getEndDate());
        newVersion.setSignDate(contract.getSignDate());
        newVersion.setContractContent(contract.getContractContent());
        newVersion.setChangeReason(changeReason != null ? changeReason : "退回修改后创建新版本");
        newVersion.setCreateBy(SecurityUtils.getCurrentUserId());
        newVersion.setCreateTime(LocalDateTime.now());

        versionMapper.insert(newVersion);

        contract.setCurrentVersion(newVersion.getVersionNo());
        contractMapper.updateById(contract);

        updateAttachmentVersion(contractId, currentVersion, newVersion.getVersionNo());

        recordVersionChange(contractId, currentVersion, newVersion.getVersionNo(),
                ContractVersionChange.CHANGE_TYPE_RETURN,
                changeReason != null ? changeReason : "退回修改",
                null);

        return newVersion;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalInstance createNewApprovalForVersion(Long contractId, Integer versionNo) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        ContractVersionApproval oldApproval = versionApprovalMapper.selectCurrentByContractId(contractId);
        if (oldApproval != null) {
            oldApproval.setIsCurrent(0);
            versionApprovalMapper.updateById(oldApproval);
        }

        String instanceName = "合同审批（V" + versionNo + "）：" + contract.getContractName();

        ApprovalInstance newInstance = approvalService.startApproval(
                contractId,
                "contract",
                instanceName,
                null
        );

        bindVersionWithApproval(contractId, versionNo, newInstance.getId());

        return newInstance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAttachmentVersion(Long contractId, Integer oldVersion, Integer newVersion) {
        List<ContractAttachment> attachments = attachmentMapper.selectByContractId(contractId);
        
        for (ContractAttachment attachment : attachments) {
            if (attachment.getVersionNo() == null || attachment.getVersionNo().equals(oldVersion)) {
                ContractAttachment newAttachment = new ContractAttachment();
                BeanUtil.copyProperties(attachment, newAttachment, "id");
                newAttachment.setVersionNo(newVersion);
                newAttachment.setCreateTime(LocalDateTime.now());
                attachmentMapper.insert(newAttachment);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordVersionChange(Long contractId, Integer fromVersion, Integer toVersion,
                                     String changeType, String changeReason, String changedFields) {
        ContractVersionChange change = new ContractVersionChange();
        change.setContractId(contractId);
        change.setFromVersionNo(fromVersion);
        change.setToVersionNo(toVersion);
        change.setChangeType(changeType);
        change.setChangeReason(changeReason);
        change.setChangedFields(changedFields);
        change.setCreateBy(SecurityUtils.getCurrentUserId());
        change.setCreateTime(LocalDateTime.now());

        versionChangeMapper.insert(change);
    }

    @Override
    public List<ContractVersionChange> getVersionChangeHistory(Long contractId) {
        return versionChangeMapper.selectByContractId(contractId);
    }

    @Override
    public ContractVersion getContractVersion(Long contractId, Integer versionNo) {
        return versionMapper.selectByContractIdAndVersion(contractId, versionNo);
    }

    @Override
    public List<ContractAttachment> getVersionAttachments(Long contractId, Integer versionNo) {
        LambdaQueryWrapper<ContractAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractAttachment::getContractId, contractId)
                .eq(ContractAttachment::getVersionNo, versionNo)
                .eq(ContractAttachment::getDelFlag, 0)
                .orderByDesc(ContractAttachment::getCreateTime);
        return attachmentMapper.selectList(wrapper);
    }
}
