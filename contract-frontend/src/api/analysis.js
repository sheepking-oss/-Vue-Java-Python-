import request from '@/utils/request'

export function extractKeyInfo(contractId, versionNo, content) {
  const params = { contractId }
  if (versionNo) {
    params.versionNo = versionNo
  }
  return request({
    url: '/contract/analysis/extract',
    method: 'post',
    params: params,
    data: { content }
  })
}

export function checkRisk(contractId, versionNo, content) {
  const params = { contractId }
  if (versionNo) {
    params.versionNo = versionNo
  }
  return request({
    url: '/contract/analysis/risk',
    method: 'post',
    params: params,
    data: { content }
  })
}

export function fullAnalysis(contractId, versionNo, content) {
  const params = { contractId }
  if (versionNo) {
    params.versionNo = versionNo
  }
  return request({
    url: '/contract/analysis/full',
    method: 'post',
    params: params,
    data: { content }
  })
}

export function getAnalysisTasks(contractId) {
  return request({
    url: `/contract/analysis/tasks/${contractId}`,
    method: 'get'
  })
}

export function validateTask(taskId, contractId) {
  return request({
    url: '/contract/analysis/validate',
    method: 'get',
    params: { taskId, contractId }
  })
}
