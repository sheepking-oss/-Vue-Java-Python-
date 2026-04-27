import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'HomeFilled' }
      },
      {
        path: 'contract',
        name: 'Contract',
        meta: { title: '合同管理', icon: 'Document' },
        children: [
          {
            path: 'list',
            name: 'ContractList',
            component: () => import('@/views/contract/list.vue'),
            meta: { title: '合同列表' }
          },
          {
            path: 'create',
            name: 'ContractCreate',
            component: () => import('@/views/contract/form.vue'),
            meta: { title: '新建合同' }
          },
          {
            path: 'edit/:id',
            name: 'ContractEdit',
            component: () => import('@/views/contract/form.vue'),
            meta: { title: '编辑合同', hidden: true }
          },
          {
            path: 'detail/:id',
            name: 'ContractDetail',
            component: () => import('@/views/contract/detail.vue'),
            meta: { title: '合同详情', hidden: true }
          }
        ]
      },
      {
        path: 'approval',
        name: 'Approval',
        meta: { title: '审批管理', icon: 'Check' },
        children: [
          {
            path: 'my-approval',
            name: 'MyApproval',
            component: () => import('@/views/approval/my-approval.vue'),
            meta: { title: '待我审批' }
          },
          {
            path: 'my-initiated',
            name: 'MyInitiated',
            component: () => import('@/views/approval/my-initiated.vue'),
            meta: { title: '我发起的' }
          },
          {
            path: 'detail/:id',
            name: 'ApprovalDetail',
            component: () => import('@/views/approval/detail.vue'),
            meta: { title: '审批详情', hidden: true }
          }
        ]
      },
      {
        path: 'archive',
        name: 'Archive',
        meta: { title: '归档管理', icon: 'Box' },
        children: [
          {
            path: 'list',
            name: 'ArchiveList',
            component: () => import('@/views/archive/list.vue'),
            meta: { title: '归档列表' }
          }
        ]
      },
      {
        path: 'report',
        name: 'Report',
        meta: { title: '统计报表', icon: 'DataAnalysis' },
        children: [
          {
            path: 'contract-stats',
            name: 'ContractStats',
            component: () => import('@/views/report/contract-stats.vue'),
            meta: { title: '合同统计' }
          },
          {
            path: 'approval-stats',
            name: 'ApprovalStats',
            component: () => import('@/views/report/approval-stats.vue'),
            meta: { title: '审批统计' }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  document.title = to.meta.title ? `${to.meta.title} - 合同管理平台` : '合同管理平台'

  if (to.meta.requiresAuth === false) {
    if (token && to.path === '/login') {
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  if (!token) {
    next('/login')
    return
  }

  if (!userStore.userInfo) {
    try {
      await userStore.getInfo()
    } catch (error) {
      await userStore.logout()
      next('/login')
      return
    }
  }

  next()
})

export default router
