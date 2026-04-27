package com.contract.modules.contract.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.common.exception.BusinessException;
import com.contract.entity.Contract;
import com.contract.entity.ContractAnalysisTask;
import com.contract.mapper.ContractAnalysisTaskMapper;
import com.contract.mapper.ContractMapper;
import com.contract.modules.contract.service.ContractAnalysisService;
import com.contract.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ContractAnalysisServiceImpl implements ContractAnalysisService {

    @Autowired
    private ContractAnalysisTaskMapper taskMapper;

    @Autowired
    private ContractMapper contractMapper;

    @Value("${python.service.url:http://localhost:8000}")
    private String pythonServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private final Map<String, TaskBinding> taskBindingCache = new ConcurrentHashMap<>();

    private static class TaskBinding {
        Long contractId;
        Integer versionNo;
        LocalDateTime createTime;

        TaskBinding(Long contractId, Integer versionNo) {
            this.contractId = contractId;
            this.versionNo = versionNo;
            this.createTime = LocalDateTime.now();
        }
    }

    @Override
    public String generateTaskId(Long contractId, Integer versionNo) {
        String prefix = "CT-" + contractId + "-V" + (versionNo != null ? versionNo : "0") + "-";
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String taskId = prefix + unique.toUpperCase();
        
        taskBindingCache.put(taskId, new TaskBinding(contractId, versionNo));
        
        log.info("Generated taskId: {} for contractId: {}, versionNo: {}", taskId, contractId, versionNo);
        return taskId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContractAnalysisTask createTask(Long contractId, Integer versionNo, String analysisType, String content) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        if (versionNo == null) {
            versionNo = contract.getCurrentVersion();
            if (versionNo == null) {
                versionNo = 1;
            }
        }

        String taskId = generateTaskId(contractId, versionNo);
        String contentHash = DigestUtil.md5Hex(content);

        ContractAnalysisTask task = new ContractAnalysisTask();
        task.setTaskId(taskId);
        task.setContractId(contractId);
        task.setVersionNo(versionNo);
        task.setAnalysisType(analysisType);
        task.setStatus(ContractAnalysisTask.STATUS_PENDING);
        task.setContentHash(contentHash);
        task.setCreateTime(LocalDateTime.now());
        task.setCreateBy(SecurityUtils.getCurrentUserId());

        taskMapper.insert(task);

        log.info("Created analysis task: {}, type: {}", taskId, analysisType);
        return task;
    }

    @Override
    public ContractAnalysisTask getTaskByTaskId(String taskId) {
        ContractAnalysisTask task = taskMapper.selectByTaskId(taskId);
        if (task == null) {
            LambdaQueryWrapper<ContractAnalysisTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ContractAnalysisTask::getTaskId, taskId);
            task = taskMapper.selectOne(wrapper);
        }
        return task;
    }

    @Override
    public List<ContractAnalysisTask> getTasksByContractId(Long contractId) {
        return taskMapper.selectByContractId(contractId);
    }

    @Override
    public ContractAnalysisTask getLatestTask(Long contractId) {
        return taskMapper.selectLatestByContractId(contractId);
    }

    @Override
    public boolean validateTaskBinding(String taskId, Long contractId) {
        TaskBinding cachedBinding = taskBindingCache.get(taskId);
        if (cachedBinding != null) {
            boolean valid = cachedBinding.contractId.equals(contractId);
            log.info("Task binding validation (cache): taskId={}, contractId={}, valid={}", 
                     taskId, contractId, valid);
            return valid;
        }

        ContractAnalysisTask task = getTaskByTaskId(taskId);
        if (task == null) {
            log.warn("Task not found for validation: taskId={}", taskId);
            return false;
        }

        boolean valid = task.getContractId().equals(contractId);
        log.info("Task binding validation (database): taskId={}, contractId={}, taskContractId={}, valid={}", 
                 taskId, contractId, task.getContractId(), valid);
        
        if (valid) {
            taskBindingCache.put(taskId, new TaskBinding(task.getContractId(), task.getVersionNo()));
        }
        
        return valid;
    }

    @Override
    public Map<String, Object> analyzeKeyInfo(Long contractId, Integer versionNo, String content) {
        return doAnalyze(contractId, versionNo, ContractAnalysisTask.TYPE_KEY_INFO, content);
    }

    @Override
    public Map<String, Object> analyzeRisk(Long contractId, Integer versionNo, String content) {
        return doAnalyze(contractId, versionNo, ContractAnalysisTask.TYPE_RISK, content);
    }

    @Override
    public Map<String, Object> analyzeFull(Long contractId, Integer versionNo, String content) {
        return doAnalyze(contractId, versionNo, ContractAnalysisTask.TYPE_FULL, content);
    }

    @Transactional(rollbackFor = Exception.class)
    private Map<String, Object> doAnalyze(Long contractId, Integer versionNo, String analysisType, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("分析内容不能为空");
        }

        ContractAnalysisTask task = createTask(contractId, versionNo, analysisType, content);
        String taskId = task.getTaskId();

        try {
            task.setStatus(ContractAnalysisTask.STATUS_PROCESSING);
            task.setStartTime(LocalDateTime.now());
            taskMapper.updateById(task);

            String endpoint = getEndpointByType(analysisType);
            String url = pythonServiceUrl + "/api/analysis" + endpoint;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("taskId", taskId);
            requestBody.put("contractId", contractId);
            requestBody.put("versionNo", versionNo);
            requestBody.put("content", content);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("Calling Python analysis service: taskId={}, url={}", taskId, url);
            
            String responseStr = restTemplate.postForObject(url, request, String.class);
            JSONObject response = JSON.parseObject(responseStr);

            if (response == null) {
                throw new BusinessException("分析服务响应为空");
            }

            String responseTaskId = response.getString("taskId");
            Long responseContractId = response.getLong("contractId");

            if (responseTaskId == null || responseContractId == null) {
                log.warn("Response missing taskId or contractId, using request values for validation");
            } else {
                if (!taskId.equals(responseTaskId)) {
                    log.error("Task ID mismatch! Request: {}, Response: {}", taskId, responseTaskId);
                    throw new BusinessException("任务标识不匹配，可能存在并发问题");
                }

                if (!validateTaskBinding(responseTaskId, responseContractId)) {
                    log.error("Task binding validation failed! taskId={}, contractId={}", responseTaskId, responseContractId);
                    throw new BusinessException("任务与合同绑定关系校验失败");
                }
            }

            Boolean success = response.getBoolean("success");
            if (success == null || !success) {
                String errorMsg = response.getString("detail");
                if (errorMsg == null) {
                    errorMsg = "分析服务执行失败";
                }
                updateTaskFailed(taskId, errorMsg);
                throw new BusinessException(errorMsg);
            }

            updateTaskSuccess(taskId, responseStr);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("taskId", taskId);
            result.put("contractId", contractId);
            result.put("versionNo", versionNo);

            if (response.containsKey("keyInfo")) {
                result.put("keyInfo", response.get("keyInfo"));
            }
            if (response.containsKey("risks")) {
                result.put("risks", response.get("risks"));
            }
            if (response.containsKey("data")) {
                result.put("data", response.get("data"));
            }

            log.info("Analysis completed successfully: taskId={}", taskId);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Analysis failed: taskId={}, error={}", taskId, e.getMessage(), e);
            updateTaskFailed(taskId, e.getMessage());
            throw new BusinessException("分析服务调用失败: " + e.getMessage());
        }
    }

    private String getEndpointByType(String analysisType) {
        switch (analysisType) {
            case ContractAnalysisTask.TYPE_KEY_INFO:
                return "/extract";
            case ContractAnalysisTask.TYPE_RISK:
                return "/risk";
            case ContractAnalysisTask.TYPE_FULL:
                return "/full";
            default:
                return "/full";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskSuccess(String taskId, String result) {
        ContractAnalysisTask task = getTaskByTaskId(taskId);
        if (task != null) {
            task.setStatus(ContractAnalysisTask.STATUS_SUCCESS);
            task.setResult(result);
            task.setEndTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
        taskBindingCache.remove(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskFailed(String taskId, String errorMsg) {
        ContractAnalysisTask task = getTaskByTaskId(taskId);
        if (task != null) {
            task.setStatus(ContractAnalysisTask.STATUS_FAILED);
            task.setErrorMsg(errorMsg);
            task.setEndTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
        taskBindingCache.remove(taskId);
    }
}
