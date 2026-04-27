package com.contract.modules.approval.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contract.entity.ApprovalComment;
import com.contract.entity.ApprovalInstance;
import com.contract.entity.ApprovalInstanceNode;
import com.contract.vo.ApprovalActionVO;
import com.contract.vo.ApprovalSubmitVO;

import java.util.List;

public interface ApprovalService {

    ApprovalInstance startApproval(Long businessId, String businessType, String instanceName, Long flowId);

    Page<ApprovalInstance> getMyApprovalList(int page, int size, String businessType, Integer status);

    Page<ApprovalInstance> getMyInitiatedList(int page, int size, String businessType, Integer status);

    ApprovalInstance getById(Long id);

    ApprovalInstance getByBusinessId(Long businessId, String businessType);

    void processApproval(ApprovalActionVO actionVO);

    List<ApprovalInstanceNode> getInstanceNodes(Long instanceId);

    List<ApprovalComment> getInstanceComments(Long instanceId);

    void withdrawApproval(Long instanceId);
}
