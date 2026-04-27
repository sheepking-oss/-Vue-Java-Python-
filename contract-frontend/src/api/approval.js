import request from '@/utils/request'

export function getMyApprovalList(params) {
  return request({
    url: '/approval/my-approval',
    method: 'get',
    params
  })
}

export function getMyInitiatedList(params) {
  return request({
    url: '/approval/my-initiated',
    method: 'get',
    params
  })
}

export function getApprovalById(id) {
  return request({
    url: `/approval/${id}`,
    method: 'get'
  })
}

export function getApprovalByBusiness(businessType, businessId) {
  return request({
    url: `/approval/business/${businessType}/${businessId}`,
    method: 'get'
  })
}

export function processApproval(data) {
  return request({
    url: '/approval/process',
    method: 'post',
    data
  })
}

export function getApprovalNodes(instanceId) {
  return request({
    url: `/approval/${instanceId}/nodes`,
    method: 'get'
  })
}

export function getApprovalComments(instanceId) {
  return request({
    url: `/approval/${instanceId}/comments`,
    method: 'get'
  })
}

export function withdrawApproval(instanceId) {
  return request({
    url: `/approval/${instanceId}/withdraw`,
    method: 'post'
  })
}
