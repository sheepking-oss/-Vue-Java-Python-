import request from '@/utils/request'

export function extractKeyInfo(content) {
  return request({
    url: '/api/analysis/extract',
    method: 'post',
    data: { content }
  })
}

export function checkRisk(content) {
  return request({
    url: '/api/analysis/risk',
    method: 'post',
    data: { content }
  })
}
