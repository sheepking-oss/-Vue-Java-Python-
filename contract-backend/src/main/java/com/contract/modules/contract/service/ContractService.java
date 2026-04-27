package com.contract.modules.contract.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contract.entity.Contract;
import com.contract.entity.ContractAttachment;
import com.contract.entity.ContractVersion;
import com.contract.vo.ContractVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContractService {

    Page<Contract> getPage(int page, int size, String contractName, String contractNo,
                            Long typeId, Integer status, Long createBy, Long deptId);

    Contract getById(Long id);

    Contract create(ContractVO contractVO);

    Contract update(Long id, ContractVO contractVO);

    void delete(Long id);

    void submitApproval(Long id);

    ContractVersion createVersion(Long contractId, String changeReason);

    List<ContractVersion> getVersions(Long contractId);

    ContractAttachment uploadAttachment(Long contractId, MultipartFile file, String description);

    List<ContractAttachment> getAttachments(Long contractId);

    void deleteAttachment(Long attachmentId);

    void archive(Long id);
}
