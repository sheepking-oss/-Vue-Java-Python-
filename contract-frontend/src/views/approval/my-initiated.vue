<template>
  <div>
    <div class="page-header">
      <span class="title">我发起的</span>
    </div>

    <div class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="业务类型">
          <el-select v-model="searchForm.businessType" placeholder="请选择类型" clearable>
            <el-option label="合同审批" value="contract" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="审批中" :value="1" />
            <el-option label="已通过" :value="2" />
            <el-option label="已驳回" :value="3" />
            <el-option label="已退回" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="instanceName" label="审批名称" min-width="250">
          <template #default="{ row }">
            <el-button type="primary" link @click="goToDetail(row.id)">
              {{ row.instanceName }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="businessType" label="业务类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">
              {{ row.businessType === 'contract' ? '合同审批' : row.businessType }}
            </el-tag>
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
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160">
          <template #default="{ row }">
            {{ row.endTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link @click="goToDetail(row.id)">
              详情
            </el-button>
            <el-button
              type="warning"
              link
              v-if="row.status === 1"
              @click="handleWithdraw(row)"
            >
              撤回
            </el-button>
            <el-button
              type="primary"
              link
              v-if="row.status === 3 || row.status === 4"
              @click="handleReSubmit(row)"
            >
              重新提交
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyInitiatedList, withdrawApproval } from '@/api/approval'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  businessType: null,
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

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

const fetchData = async () => {
  loading.value = true
  try {
    tableData.value = [
      {
        id: 1,
        instanceName: '软件采购合同审批',
        businessType: 'contract',
        currentNodeName: '法务审核',
        status: 1,
        startTime: '2026-04-27 10:00:00',
        endTime: null
      },
      {
        id: 2,
        instanceName: '办公设备租赁合同审批',
        businessType: 'contract',
        currentNodeName: null,
        status: 3,
        startTime: '2026-04-25 11:00:00',
        endTime: '2026-04-25 16:30:00'
      }
    ]
    pagination.total = 2
  } catch (error) {
    console.error('Fetch error:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.businessType = null
  searchForm.status = null
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  fetchData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  fetchData()
}

const goToDetail = (id) => {
  router.push(`/approval/detail/${id}`)
}

const handleWithdraw = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要撤回"${row.instanceName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await withdrawApproval(row.id)
    ElMessage.success('撤回成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Withdraw error:', error)
    }
  }
}

const handleReSubmit = (row) => {
  ElMessage.info('请前往合同详情页重新提交审批')
}

onMounted(() => {
  fetchData()
})
</script>
