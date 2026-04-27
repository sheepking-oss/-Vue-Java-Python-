import { defineStore } from 'pinia'
import { login, logout, getInfo } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    roles: [],
    permissions: [],
    menus: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    hasPermission: (state) => (permission) => {
      return state.permissions.includes(permission) || state.roles.includes('admin')
    }
  },

  actions: {
    async login(loginForm) {
      const res = await login(loginForm)
      this.token = res.data.token
      this.userInfo = {
        userId: res.data.userId,
        username: res.data.username,
        nickName: res.data.nickName,
        avatar: res.data.avatar,
        deptId: res.data.deptId,
        deptName: res.data.deptName
      }
      this.roles = res.data.roles || []
      this.permissions = res.data.permissions || []
      this.menus = res.data.menus || []

      localStorage.setItem('token', res.data.token)
      return res
    },

    async getInfo() {
      const res = await getInfo()
      this.userInfo = {
        userId: res.data.userId,
        username: res.data.username,
        nickName: res.data.nickName,
        avatar: res.data.avatar,
        deptId: res.data.deptId,
        deptName: res.data.deptName
      }
      this.roles = res.data.roles || []
      this.permissions = res.data.permissions || []
      this.menus = res.data.menus || []
      return res
    },

    async logout() {
      try {
        await logout()
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        this.token = ''
        this.userInfo = null
        this.roles = []
        this.permissions = []
        this.menus = []
        localStorage.removeItem('token')
      }
    }
  }
})
