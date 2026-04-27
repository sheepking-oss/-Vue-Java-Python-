package com.contract.modules.contract.service;

import com.contract.entity.ContractAnalysisTask;

import java.util.List;
import java.util.Map;

public interface ContractAnalysisService {

    String generateTaskId(Long contractId, Integer versionNo);

    ContractAnalysisTask createTask(Long contractId, Integer versionNo, String analysisType, String content);

    ContractAnalysisTask getTaskByTaskId(String taskId);

    List<ContractAnalysisTask> getTasksByContractId(Long contractId);

    ContractAnalysisTask getLatestTask(Long contractId);

    boolean validateTaskBinding(String taskId, Long contractId);

    Map<String, Object> analyzeKeyInfo(Long contractId, Integer versionNo, String content);

    Map<String, Object> analyzeRisk(Long contractId, Integer versionNo, String content);

    Map<String, Object> analyzeFull(Long contractId, Integer versionNo, String content);

    void updateTaskSuccess(String taskId, String result);

    void updateTaskFailed(String taskId, String errorMsg);
}
