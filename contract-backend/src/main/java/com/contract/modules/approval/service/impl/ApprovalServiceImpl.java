package com.contract.modules.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contract.common.exception.BusinessException;
import com.contract.entity.*;
import com.contract.mapper.*;
import com.contract.modules.approval.service.ApprovalService;
import com.contract.modules.contract.service.ContractService;
import com.contract.modules.contract.service.ContractVersionApprovalService;
import com.contract.utils.SecurityUtils;
import com.contract.vo.ApprovalActionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    @Autowired
    private ApprovalInstanceMapper instanceMapper;

    @Autowired
    private ApprovalNodeMapper nodeMapper;

    @Autowired
    private ApprovalFlowMapper flowMapper;

    @Autowired
    private ApprovalInstanceNodeMapper instanceNodeMapper;

    @Autowired
    private ApprovalCommentMapper commentMapper;

    @Autowired
    private ContractMapper contractMapper;

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractVersionApprovalService versionApprovalService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalInstance startApproval(Long businessId, String businessType, String instanceName, Long flowId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        if (flowId == null) {
            ApprovalFlow defaultFlow = flowMapper.selectByFlowCode("CONTRACT_APPROVAL");
            if (defaultFlow == null) {
                throw new BusinessException("未找到审批流程定义");
            }
            flowId = defaultFlow.getId();
        }

        List<ApprovalNode> nodes = nodeMapper.selectByFlowId(flowId);
        if (nodes == null || nodes.isEmpty()) {
            throw new BusinessException("审批流程节点未定义");
        }

        nodes.sort(Comparator.comparingInt(ApprovalNode::getSort));

        Integer contractVersionNo = null;
        if ("contract".equals(businessType)) {
            Contract contract = contractMapper.selectById(businessId);
            if (contract != null) {
                contractVersionNo = contract.getCurrentVersion();
                if (contractVersionNo == null) {
                    contractVersionNo = 1;
                }
            }
        }

        ApprovalInstance instance = new ApprovalInstance();
        instance.setBusinessId(businessId);
        instance.setBusinessType(businessType);
        instance.setContractVersionNo(contractVersionNo);
        instance.setFlowId(flowId);
        instance.setInstanceName(instanceName);
        instance.setStatus(ApprovalInstance.STATUS_APPROVING);
        instance.setStartTime(LocalDateTime.now());
        instance.setCreateBy(userId);
        instance.setCreateTime(LocalDateTime.now());

        ApprovalNode firstNode = nodes.stream()
                .filter(n -> n.getSort() > 1)
                .findFirst()
                .orElse(null);

        if (firstNode != null) {
            instance.setCurrentNodeId(firstNode.getId());
            instance.setCurrentNodeName(firstNode.getNodeName());
        }

        instanceMapper.insert(instance);

        for (ApprovalNode node : nodes) {
            ApprovalInstanceNode instanceNode = new ApprovalInstanceNode();
            instanceNode.setInstanceId(instance.getId());
            instanceNode.setNodeId(node.getId());
            instanceNode.setContractVersionNo(contractVersionNo);
            instanceNode.setNodeName(node.getNodeName());
            instanceNode.setNodeType(node.getNodeType());
            instanceNode.setSort(node.getSort());
            instanceNode.setAssigneeType(node.getAssigneeType());
            instanceNode.setAssigneeValue(node.getAssigneeValue());
            instanceNode.setStatus(ApprovalInstanceNode.STATUS_PENDING);
            instanceNodeMapper.insert(instanceNode);
        }

        return instance;
    }

    @Override
    public Page<ApprovalInstance> getMyApprovalList(int page, int size, String businessType, Integer status) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        Page<ApprovalInstance> pageParam = new Page<>(page, size);
        return instanceMapper.selectMyApprovalList(pageParam, userId, businessType, status);
    }

    @Override
    public Page<ApprovalInstance> getMyInitiatedList(int page, int size, String businessType, Integer status) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        Page<ApprovalInstance> pageParam = new Page<>(page, size);
        return instanceMapper.selectMyInitiatedList(pageParam, userId, businessType, status);
    }

    @Override
    public ApprovalInstance getById(Long id) {
        return instanceMapper.selectByIdWithDetails(id);
    }

    @Override
    public ApprovalInstance getByBusinessId(Long businessId, String businessType) {
        if ("contract".equals(businessType)) {
            ContractVersionApproval versionApproval = versionApprovalService.getCurrentVersionApproval(businessId);
            if (versionApproval != null) {
                return instanceMapper.selectById(versionApproval.getInstanceId());
            }
        }
        return instanceMapper.selectByBusinessId(businessId, businessType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processApproval(ApprovalActionVO actionVO) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        ApprovalInstance instance = instanceMapper.selectById(actionVO.getInstanceId());
        if (instance == null) {
            throw new BusinessException("审批实例不存在");
        }

        if (instance.getStatus() != ApprovalInstance.STATUS_APPROVING) {
            throw new BusinessException("审批实例状态不正确");
        }

        List<ApprovalInstanceNode> instanceNodes = instanceNodeMapper.selectByInstanceId(instance.getId());
        instanceNodes.sort(Comparator.comparingInt(ApprovalInstanceNode::getSort));

        ApprovalInstanceNode currentNode = instanceNodes.stream()
                .filter(n -> n.getStatus() == ApprovalInstanceNode.STATUS_PENDING)
                .findFirst()
                .orElse(null);

        if (currentNode == null) {
            throw new BusinessException("没有待审批的节点");
        }

        boolean hasPermission = checkApprovalPermission(currentNode, userId);
        if (!hasPermission) {
            throw new BusinessException("您没有权限审批此节点");
        }

        currentNode.setActualAssignee(userId);
        currentNode.setEndTime(LocalDateTime.now());
        currentNode.setComment(actionVO.getComment());

        ApprovalComment comment = new ApprovalComment();
        comment.setInstanceId(instance.getId());
        comment.setNodeId(currentNode.getNodeId());
        comment.setContractVersionNo(instance.getContractVersionNo());
        comment.setApproverId(userId);
        comment.setActionType(actionVO.getActionType());
        comment.setComment(actionVO.getComment());
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        if (actionVO.getActionType() == ApprovalComment.ACTION_PASS) {
            currentNode.setStatus(ApprovalInstanceNode.STATUS_PASSED);
            instanceNodeMapper.updateById(currentNode);

            ApprovalInstanceNode nextNode = instanceNodes.stream()
                    .filter(n -> n.getSort() > currentNode.getSort() && n.getNodeType() != ApprovalNode.NODE_TYPE_END)
                    .findFirst()
                    .orElse(null);

            if (nextNode != null) {
                instance.setCurrentNodeId(nextNode.getNodeId());
                instance.setCurrentNodeName(nextNode.getNodeName());
                nextNode.setStartTime(LocalDateTime.now());
                nextNode.setStatus(ApprovalInstanceNode.STATUS_PROCESSING);
                instanceNodeMapper.updateById(nextNode);
            } else {
                instance.setStatus(ApprovalInstance.STATUS_PASSED);
                instance.setEndTime(LocalDateTime.now());
                instance.setCurrentNodeId(null);
                instance.setCurrentNodeName(null);

                updateBusinessStatus(instance, Contract.STATUS_PASSED);
            }
        } else if (actionVO.getActionType() == ApprovalComment.ACTION_REJECT) {
            currentNode.setStatus(ApprovalInstanceNode.STATUS_REJECTED);
            instanceNodeMapper.updateById(currentNode);

            instance.setStatus(ApprovalInstance.STATUS_REJECTED);
            instance.setEndTime(LocalDateTime.now());
            instance.setCurrentNodeId(null);
            instance.setCurrentNodeName(null);

            updateBusinessStatus(instance, Contract.STATUS_REJECTED);
        } else if (actionVO.getActionType() == ApprovalComment.ACTION_RETURN) {
            currentNode.setStatus(ApprovalInstanceNode.STATUS_RETURNED);
            instanceNodeMapper.updateById(currentNode);

            for (ApprovalInstanceNode node : instanceNodes) {
                if (node.getSort() <= currentNode.getSort()) {
                    node.setStatus(ApprovalInstanceNode.STATUS_PENDING);
                    instanceNodeMapper.updateById(node);
                }
            }

            instance.setStatus(ApprovalInstance.STATUS_RETURNED);
            instance.setEndTime(LocalDateTime.now());
            instance.setCurrentNodeId(null);
            instance.setCurrentNodeName(null);

            updateBusinessStatus(instance, Contract.STATUS_RETURNED);
        }

        instanceMapper.updateById(instance);
    }

    @Override
    public List<ApprovalInstanceNode> getInstanceNodes(Long instanceId) {
        return instanceNodeMapper.selectByInstanceId(instanceId);
    }

    @Override
    public List<ApprovalComment> getInstanceComments(Long instanceId) {
        return commentMapper.selectByInstanceId(instanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawApproval(Long instanceId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        ApprovalInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException("审批实例不存在");
        }

        if (!instance.getCreateBy().equals(userId)) {
            throw new BusinessException("只能撤回自己发起的审批");
        }

        if (instance.getStatus() != ApprovalInstance.STATUS_APPROVING) {
            throw new BusinessException("只能撤回审批中的实例");
        }

        instance.setStatus(ApprovalInstance.STATUS_CANCELLED);
        instance.setEndTime(LocalDateTime.now());
        instanceMapper.updateById(instance);

        ApprovalComment comment = new ApprovalComment();
        comment.setInstanceId(instance.getId());
        comment.setContractVersionNo(instance.getContractVersionNo());
        comment.setApproverId(userId);
        comment.setActionType(ApprovalComment.ACTION_WITHDRAW);
        comment.setComment("发起人撤回审批");
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        updateBusinessStatus(instance, Contract.STATUS_DRAFT);
    }

    private boolean checkApprovalPermission(ApprovalInstanceNode node, Long userId) {
        if (node.getAssigneeType() == null) {
            return true;
        }

        switch (node.getAssigneeType()) {
            case ApprovalNode.ASSIGNEE_TYPE_USER:
                if (node.getAssigneeValue() != null) {
                    String[] userIds = node.getAssigneeValue().split(",");
                    for (String id : userIds) {
                        if (userId.toString().equals(id.trim())) {
                            return true;
                        }
                    }
                }
                return false;
            case ApprovalNode.ASSIGNEE_TYPE_ROLE:
                return true;
            case ApprovalNode.ASSIGNEE_TYPE_DEPT_LEADER:
                return true;
            case ApprovalNode.ASSIGNEE_TYPE_SELECT:
                return true;
            default:
                return true;
        }
    }

    private void updateBusinessStatus(ApprovalInstance instance, int status) {
        if ("contract".equals(instance.getBusinessType())) {
            Contract contract = contractMapper.selectById(instance.getBusinessId());
            if (contract != null) {
                contract.setStatus(status);
                contractMapper.updateById(contract);
            }
        }
    }
}
