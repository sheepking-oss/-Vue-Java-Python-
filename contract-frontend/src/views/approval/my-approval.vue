<template>
  <div>
    <div class="page-header">
      <span class="title">待我审批</span>
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
        <el-table-column prop="createByName" label="发起人" width="100" />
        <el-table-column prop="createTime" label="发起时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleApprove(row)">
              审批
            </el-button>
            <el-button type="info" link @click="goToDetail(row.id)">
              详情
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

    <el-dialog
      v-model="approveDialogVisible"
      title="审批处理"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="操作">
          <el-radio-group v-model="approveForm.actionType">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="2">驳回</el-radio>
            <el-radio :value="3">退回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="退回节点" v-if="approveForm.actionType === 3">
          <el-select v-model="approveForm.returnNodeId" placeholder="请选择退回节点" style="width: 100%;">
            <el-option
              v-for="node in approvalNodes"
              :key="node.id"
              :label="node.nodeName"
              :value="node.id"
              v-if="node.sort < currentNodeSort"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input
            v-model="approveForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="approveLoading" @click="handleSubmitApprove">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyApprovalList, processApproval, getApprovalNodes } from '@/api/approval'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const approveDialogVisible = ref(false)
const approveLoading = ref(false)
const currentApproval = ref(null)
const approvalNodes = ref([])
const currentNodeSort = ref(0)

const searchForm = reactive({
  businessType: null,
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const approveForm = reactive({
  instanceId: null,
  nodeId: null,
  actionType: 1,
  comment: '',
  returnNodeId: null
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
        createByName: '王销售',
        createTime: '2026-04-27 10:00:00'
      },
      {
        id: 2,
        instanceName: '设备采购合同审批',
        businessType: 'contract',
        currentNodeName: '财务审核',
        status: 1,
        createByName: '李采购',
        createTime: '2026-04-26 15:30:00'
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

const handleApprove = async (row) => {
  currentApproval.value = row
  approveForm.instanceId = row.id
  approveForm.actionType = 1
  approveForm.comment = ''
  approveForm.returnNodeId = null
  
  try {
    const nodes = await getApprovalNodes(row.id)
    approvalNodes.value = nodes.data || []
  } catch (error) {
    approvalNodes.value = [
      { id: 1, nodeName: '发起人提交', sort: 1 },
      { id: 2, nodeName: '部门经理审批', sort: 2 },
      { id: 3, nodeName: '法务审核', sort: 3 },
      { id: 4, nodeName: '财务审核', sort: 4 },
      { id: 5, nodeName: '总经理审批', sort: 5 }
    ]
  }
  
  currentNodeSort.value = 3
  approveDialogVisible.value = true
}

const handleSubmitApprove = async () => {
  approveLoading.value = true
  try {
    await processApproval(approveForm)
    ElMessage.success('审批成功')
    approveDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('Approve error:', error)
  } finally {
    approveLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>
