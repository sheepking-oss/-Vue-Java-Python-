package com.contract.modules.contract.service;

import com.contract.entity.*;

import java.util.List;

public interface ContractVersionApprovalService {

    ContractVersionApproval bindVersionWithApproval(Long contractId, Integer versionNo, Long instanceId);

    ContractVersionApproval getCurrentVersionApproval(Long contractId);

    ContractVersionApproval getVersionApproval(Long contractId, Integer versionNo);

    ContractVersion createNewVersionFromReturn(Long contractId, String changeReason);

    ApprovalInstance createNewApprovalForVersion(Long contractId, Integer versionNo);

    void updateAttachmentVersion(Long contractId, Integer oldVersion, Integer newVersion);

    void recordVersionChange(Long contractId, Integer fromVersion, Integer toVersion, 
                              String changeType, String changeReason, String changedFields);

    List<ContractVersionChange> getVersionChangeHistory(Long contractId);

    ContractVersion getContractVersion(Long contractId, Integer versionNo);

    List<ContractAttachment> getVersionAttachments(Long contractId, Integer versionNo);
}
