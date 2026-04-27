package com.contract.modules.contract.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contract.common.exception.BusinessException;
import com.contract.entity.*;
import com.contract.mapper.*;
import com.contract.modules.approval.service.ApprovalService;
import com.contract.modules.contract.service.ContractService;
import com.contract.utils.SecurityUtils;
import com.contract.vo.ContractVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractMapper contractMapper;

    @Autowired
    private ContractVersionMapper versionMapper;

    @Autowired
    private ContractAttachmentMapper attachmentMapper;

    @Autowired
    private ContractTypeMapper typeMapper;

    @Autowired
    private ApprovalService approvalService;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public Page<Contract> getPage(int page, int size, String contractName, String contractNo,
                                    Long typeId, Integer status, Long createBy, Long deptId) {
        Page<Contract> pageParam = new Page<>(page, size);
        return contractMapper.selectPageWithDetails(pageParam, contractName, contractNo, typeId, status, createBy, deptId);
    }

    @Override
    public Contract getById(Long id) {
        return contractMapper.selectByIdWithDetails(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Contract create(ContractVO contractVO) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        Contract contract = new Contract();
        BeanUtils.copyProperties(contractVO, contract);

        contract.setContractNo(generateContractNo());
        contract.setCurrentVersion(1);
        contract.setStatus(Contract.STATUS_DRAFT);
        contract.setCreateBy(userId);
        contract.setDelFlag(0);

        if (contract.getCurrency() == null) {
            contract.setCurrency("CNY");
        }
        if (contract.getContractAmount() == null) {
            contract.setContractAmount(BigDecimal.ZERO);
        }

        contractMapper.insert(contract);

        createVersion(contract.getId(), "初始版本");

        return contract;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Contract update(Long id, ContractVO contractVO) {
        Contract existing = contractMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("合同不存在");
        }

        if (existing.getStatus() != Contract.STATUS_DRAFT && existing.getStatus() != Contract.STATUS_RETURNED) {
            throw new BusinessException("只能修改草稿状态或退回状态的合同");
        }

        BeanUtils.copyProperties(contractVO, existing, "id", "contractNo", "createBy", "createTime");

        contractMapper.updateById(existing);

        return existing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            return;
        }

        if (contract.getStatus() != Contract.STATUS_DRAFT) {
            throw new BusinessException("只能删除草稿状态的合同");
        }

        contractMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApproval(Long id) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        if (contract.getStatus() != Contract.STATUS_DRAFT && contract.getStatus() != Contract.STATUS_RETURNED) {
            throw new BusinessException("只能提交草稿状态或退回状态的合同");
        }

        approvalService.startApproval(id, "contract", "合同审批：" + contract.getContractName(), null);

        contract.setStatus(Contract.STATUS_APPROVING);
        contractMapper.updateById(contract);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContractVersion createVersion(Long contractId, String changeReason) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        Integer maxVersion = versionMapper.selectMaxVersionByContractId(contractId);
        int newVersion = (maxVersion == null) ? 1 : maxVersion + 1;

        ContractVersion version = new ContractVersion();
        version.setContractId(contractId);
        version.setVersionNo(newVersion);
        version.setContractNo(contract.getContractNo());
        version.setContractName(contract.getContractName());
        version.setTypeId(contract.getTypeId());
        version.setPartyA(contract.getPartyA());
        version.setPartyB(contract.getPartyB());
        version.setContractAmount(contract.getContractAmount());
        version.setStartDate(contract.getStartDate());
        version.setEndDate(contract.getEndDate());
        version.setSignDate(contract.getSignDate());
        version.setContractContent(contract.getContractContent());
        version.setChangeReason(changeReason);
        version.setCreateBy(SecurityUtils.getCurrentUserId());
        version.setCreateTime(LocalDateTime.now());

        versionMapper.insert(version);

        contract.setCurrentVersion(newVersion);
        contractMapper.updateById(contract);

        return version;
    }

    @Override
    public List<ContractVersion> getVersions(Long contractId) {
        return versionMapper.selectByContractId(contractId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContractAttachment uploadAttachment(Long contractId, MultipartFile file, String description) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.extName(originalFilename);
        String newFileName = IdUtil.simpleUUID() + "." + extension;

        String datePath = DateUtil.format(LocalDate.now(), "yyyy/MM/dd");
        String relativePath = "contract/" + datePath + "/" + newFileName;
        String fullPath = uploadPath + relativePath;

        File destFile = new File(fullPath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }

        ContractAttachment attachment = new ContractAttachment();
        attachment.setContractId(contractId);
        attachment.setVersionId(null);
        attachment.setFileName(originalFilename);
        attachment.setFilePath(relativePath);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(extension);
        attachment.setDescription(description);
        attachment.setCreateBy(SecurityUtils.getCurrentUserId());
        attachment.setCreateTime(LocalDateTime.now());
        attachment.setDelFlag(0);

        attachmentMapper.insert(attachment);

        return attachment;
    }

    @Override
    public List<ContractAttachment> getAttachments(Long contractId) {
        return attachmentMapper.selectByContractId(contractId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long attachmentId) {
        ContractAttachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment != null) {
            attachment.setDelFlag(1);
            attachmentMapper.updateById(attachment);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archive(Long id) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        if (contract.getStatus() != Contract.STATUS_PASSED) {
            throw new BusinessException("只能归档已审批通过的合同");
        }

        contract.setStatus(Contract.STATUS_ARCHIVED);
        contractMapper.updateById(contract);

        ArchiveRecord archive = new ArchiveRecord();
        archive.setContractId(contract.getId());
        archive.setContractNo(contract.getContractNo());
        archive.setContractName(contract.getContractName());
        archive.setArchiveNo(generateArchiveNo());
        archive.setArchiveType(ArchiveRecord.TYPE_ELECTRONIC);
        archive.setArchiveTime(LocalDateTime.now());
        archive.setArchiveBy(SecurityUtils.getCurrentUserId());
        archive.setStatus(ArchiveRecord.STATUS_ARCHIVED);
        archive.setCreateTime(LocalDateTime.now());
    }

    private String generateContractNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%06d", (int) (Math.random() * 1000000));
        return "CT-" + dateStr + "-" + randomStr;
    }

    private String generateArchiveNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%06d", (int) (Math.random() * 1000000));
        return "AR-" + dateStr + "-" + randomStr;
    }
}
