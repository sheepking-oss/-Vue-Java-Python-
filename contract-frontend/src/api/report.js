import request from '@/utils/request'

export function getContractStatsByType() {
  return request({
    url: '/report/contract/by-type',
    method: 'get'
  })
}

export function getContractStatsByStatus() {
  return request({
    url: '/report/contract/by-status',
    method: 'get'
  })
}

export function getContractStatsByMonth(params) {
  return request({
    url: '/report/contract/by-month',
    method: 'get',
    params
  })
}

export function getContractStatsByDept() {
  return request({
    url: '/report/contract/by-dept',
    method: 'get'
  })
}
