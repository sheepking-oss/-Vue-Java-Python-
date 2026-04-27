import request from '@/utils/request'

export function getContractList(params) {
  return request({
    url: '/contract/list',
    method: 'get',
    params
  })
}

export function getContractById(id) {
  return request({
    url: `/contract/${id}`,
    method: 'get'
  })
}

export function createContract(data) {
  return request({
    url: '/contract/create',
    method: 'post',
    data
  })
}

export function updateContract(id, data) {
  return request({
    url: `/contract/${id}`,
    method: 'put',
    data
  })
}

export function deleteContract(id) {
  return request({
    url: `/contract/${id}`,
    method: 'delete'
  })
}

export function submitApproval(id) {
  return request({
    url: `/contract/${id}/submit`,
    method: 'post'
  })
}

export function createVersion(id, data) {
  return request({
    url: `/contract/${id}/version`,
    method: 'post',
    params: data
  })
}

export function getVersions(id) {
  return request({
    url: `/contract/${id}/versions`,
    method: 'get'
  })
}

export function uploadAttachment(id, file, description) {
  const formData = new FormData()
  formData.append('file', file)
  if (description) {
    formData.append('description', description)
  }
  
  return request({
    url: `/contract/${id}/attachment`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getAttachments(id) {
  return request({
    url: `/contract/${id}/attachments`,
    method: 'get'
  })
}

export function deleteAttachment(attachmentId) {
  return request({
    url: `/contract/attachment/${attachmentId}`,
    method: 'delete'
  })
}

export function archiveContract(id) {
  return request({
    url: `/contract/${id}/archive`,
    method: 'post'
  })
}
