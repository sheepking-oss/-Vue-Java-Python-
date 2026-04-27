import request from '@/utils/request'

export function getArchiveList(params) {
  return request({
    url: '/archive/list',
    method: 'get',
    params
  })
}

export function getArchiveById(id) {
  return request({
    url: `/archive/${id}`,
    method: 'get'
  })
}
