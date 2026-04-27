<template>
  <div>
    <div class="page-header">
      <span class="title">合同列表</span>
      <el-button type="primary" @click="goToCreate">
        <el-icon><Plus /></el-icon>
        新建合同
      </el-button>
    </div>

    <div class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="合同名称">
          <el-input v-model="searchForm.contractName" placeholder="请输入合同名称" clearable />
        </el-form-item>
        <el-form-item label="合同编号">
          <el-input v-model="searchForm.contractNo" placeholder="请输入合同编号" clearable />
        </el-form-item>
        <el-form-item label="合同状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="审批中" :value="1" />
            <el-option label="已通过" :value="2" />
            <el-option label="已驳回" :value="3" />
            <el-option label="已退回" :value="4" />
            <el-option label="已归档" :value="5" />
            <el-option label="已到期" :value="6" />
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
        <el-table-column prop="contractName" label="合同名称" min-width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="goToDetail(row.id)">{{ row.contractName }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="contractNo" label="合同编号" width="180" />
        <el-table-column prop="typeName" label="合同类型" width="100" />
        <el-table-column prop="partyA" label="甲方" width="150">
          <template #default="{ row }">
            <el-text line-clamp="1">{{ row.partyA }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="partyB" label="乙方" width="150">
          <template #default="{ row }">
            <el-text line-clamp="1">{{ row.partyB }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="contractAmount" label="合同金额" width="120">
          <template #default="{ row }">
            {{ formatAmount(row.contractAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="110" />
        <el-table-column prop="endDate" label="结束日期" width="110" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createByName" label="创建人" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goToDetail(row.id)">详情</el-button>
            <el-button
              type="primary"
              link
              v-if="row.status === 0 || row.status === 4"
              @click="goToEdit(row.id)"
            >
              编辑
            </el-button>
            <el-button
              type="success"
              link
              v-if="row.status === 0 || row.status === 4"
              @click="handleSubmit(row)"
            >
              提交审批
            </el-button>
            <el-button
              type="danger"
              link
              v-if="row.status === 0"
              @click="handleDelete(row)"
            >
              删除
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
import { getContractList, submitApproval, deleteContract } from '@/api/contract'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  contractName: '',
  contractNo: '',
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
    4: 'warning',
    5: 'primary',
    6: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
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

const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '¥0.00'
  return '¥' + Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        contractName: '软件采购合同',
        contractNo: 'CT-20260427-000001',
        typeName: '采购合同',
        partyA: '科技有限公司',
        partyB: '软件供应商',
        contractAmount: 500000,
        startDate: '2026-05-01',
        endDate: '2027-04-30',
        status: 2,
        createByName: '王销售',
        createTime: '2026-04-27 10:00:00'
      },
      {
        id: 2,
        contractName: '技术服务协议',
        contractNo: 'CT-20260426-000002',
        typeName: '服务合同',
        partyA: '科技有限公司',
        partyB: '技术服务公司',
        contractAmount: 200000,
        startDate: '2026-06-01',
        endDate: '2026-12-31',
        status: 1,
        createByName: '张法务',
        createTime: '2026-04-26 15:30:00'
      },
      {
        id: 3,
        contractName: '办公设备租赁合同',
        contractNo: 'CT-20260425-000003',
        typeName: '租赁合同',
        partyA: '科技有限公司',
        partyB: '租赁公司',
        contractAmount: 50000,
        startDate: '2026-07-01',
        endDate: '2027-06-30',
        status: 0,
        createByName: '李财务',
        createTime: '2026-04-25 11:20:00'
      }
    ]
    pagination.total = 3
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
  searchForm.contractName = ''
  searchForm.contractNo = ''
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

const goToCreate = () => {
  router.push('/contract/create')
}

const goToDetail = (id) => {
  router.push(`/contract/detail/${id}`)
}

const goToEdit = (id) => {
  router.push(`/contract/edit/${id}`)
}

const handleSubmit = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要提交合同"${row.contractName}"进行审批吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await submitApproval(row.id)
    ElMessage.success('提交审批成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Submit error:', error)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除合同"${row.contractName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteContract(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete error:', error)
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>
