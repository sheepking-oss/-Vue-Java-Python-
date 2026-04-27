<template>
  <div>
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <div class="stat-card">
          <div class="title">待我审批</div>
          <div class="value text-primary">{{ stats.pendingApproval }}</div>
          <div class="trend text-primary">
            <el-icon><TrendCharts /></el-icon>
            <span>查看详情</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="title">我发起的</div>
          <div class="value text-success">{{ stats.myInitiated }}</div>
          <div class="trend text-success">
            <el-icon><TrendCharts /></el-icon>
            <span>查看详情</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="title">合同总数</div>
          <div class="value text-warning">{{ stats.totalContracts }}</div>
          <div class="trend text-warning">
            <el-icon><TrendCharts /></el-icon>
            <span>查看详情</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="title">即将到期</div>
          <div class="value text-danger">{{ stats.expiringSoon }}</div>
          <div class="trend text-danger">
            <el-icon><TrendCharts /></el-icon>
            <span>查看详情</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="table-container">
          <div class="page-header">
            <span class="title">待我审批列表</span>
            <el-button type="primary" link @click="goToApproval">查看全部</el-button>
          </div>
          <el-table :data="pendingApprovals" style="width: 100%">
            <el-table-column prop="instanceName" label="审批名称" min-width="200" />
            <el-table-column prop="businessType" label="业务类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.businessType === 'contract' ? '合同审批' : row.businessType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="currentNodeName" label="当前节点" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发起时间" width="160" />
          </el-table>
        </div>
      </el-col>

      <el-col :span="12">
        <div class="table-container">
          <div class="page-header">
            <span class="title">最近合同</span>
            <el-button type="primary" link @click="goToContract">查看全部</el-button>
          </div>
          <el-table :data="recentContracts" style="width: 100%">
            <el-table-column prop="contractName" label="合同名称" min-width="200" />
            <el-table-column prop="contractNo" label="合同编号" width="150" />
            <el-table-column prop="partyA" label="甲方" width="100">
              <template #default="{ row }">
                <el-text line-clamp="1">{{ row.partyA }}</el-text>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getContractStatusType(row.status)" size="small">
                  {{ getContractStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const stats = ref({
  pendingApproval: 0,
  myInitiated: 0,
  totalContracts: 0,
  expiringSoon: 0
})

const pendingApprovals = ref([])
const recentContracts = ref([])

const getStatusType = (status) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    0: '已撤销',
    1: '审批中',
    2: '已通过',
    3: '已驳回',
    4: '已退回'
  }
  return texts[status] || '未知'
}

const getContractStatusType = (status) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'warning',
    5: 'primary',
    6: 'danger'
  }
  return types[status] || 'info'
}

const getContractStatusText = (status) => {
  const texts = {
    0: '草稿',
    1: '审批中',
    2: '已通过',
    3: '已驳回',
    4: '已退回',
    5: '已归档',
    6: '已到期'
  }
  return texts[status] || '未知'
}

const goToApproval = () => {
  router.push('/approval/my-approval')
}

const goToContract = () => {
  router.push('/contract/list')
}

onMounted(() => {
  stats.value = {
    pendingApproval: 3,
    myInitiated: 5,
    totalContracts: 28,
    expiringSoon: 2
  }

  pendingApprovals.value = [
    {
      id: 1,
      instanceName: '销售合同审批：测试合同001',
      businessType: 'contract',
      currentNodeName: '法务审核',
      status: 1,
      createTime: '2026-04-27 10:30:00'
    },
    {
      id: 2,
      instanceName: '采购合同审批：设备采购',
      businessType: 'contract',
      currentNodeName: '财务审核',
      status: 1,
      createTime: '2026-04-26 14:20:00'
    },
    {
      id: 3,
      instanceName: '服务合同审批：技术服务',
      businessType: 'contract',
      currentNodeName: '部门经理审批',
      status: 1,
      createTime: '2026-04-25 09:15:00'
    }
  ]

  recentContracts.value = [
    {
      id: 1,
      contractName: '软件采购合同',
      contractNo: 'CT-20260427-000001',
      partyA: '科技有限公司',
      partyB: '软件供应商',
      status: 2,
      createTime: '2026-04-27 10:00:00'
    },
    {
      id: 2,
      contractName: '技术服务协议',
      contractNo: 'CT-20260426-000002',
      partyA: '科技有限公司',
      partyB: '技术服务公司',
      status: 1,
      createTime: '2026-04-26 15:30:00'
    },
    {
      id: 3,
      contractName: '办公设备租赁合同',
      contractNo: 'CT-20260425-000003',
      partyA: '科技有限公司',
      partyB: '租赁公司',
      status: 0,
      createTime: '2026-04-25 11:20:00'
    }
  ]
})
</script>
