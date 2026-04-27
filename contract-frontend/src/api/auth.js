import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/system/auth/login',
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/system/auth/logout',
    method: 'post'
  })
}

export function getInfo() {
  return request({
    url: '/system/auth/info',
    method: 'get'
  })
}
