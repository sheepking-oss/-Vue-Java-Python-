<template>
  <div>
    <div class="page-header">
      <span class="title">归档列表</span>
    </div>

    <div class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="合同名称">
          <el-input v-model="searchForm.contractName" placeholder="请输入合同名称" clearable />
        </el-form-item>
        <el-form-item label="合同编号">
          <el-input v-model="searchForm.contractNo" placeholder="请输入合同编号" clearable />
        </el-form-item>
        <el-form-item label="归档编号">
          <el-input v-model="searchForm.archiveNo" placeholder="请输入归档编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="已归档" :value="1" />
            <el-option label="已借出" :value="2" />
            <el-option label="已归还" :value="3" />
            <el-option label="已销毁" :value="4" />
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
        <el-table-column prop="contractName" label="合同名称" min-width="200" />
        <el-table-column prop="contractNo" label="合同编号" width="180" />
        <el-table-column prop="archiveNo" label="归档编号" width="180" />
        <el-table-column prop="archiveType" label="归档类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">
              {{ row.archiveType === 1 ? '电子归档' : '纸质归档' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="storageLocation" label="存放位置" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="archiveByName" label="归档人" width="100" />
        <el-table-column prop="archiveTime" label="归档时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看
            </el-button>
            <el-button
              type="warning"
              link
              v-if="row.status === 1"
              @click="handleBorrow(row)"
            >
              借阅
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
import { ElMessage } from 'element-plus'
import { getArchiveList } from '@/api/archive'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  contractName: '',
  contractNo: '',
  archiveNo: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const getStatusType = (status) => {
  const types = {
    1: 'primary',
    2: 'warning',
    3: 'success',
    4: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    1: '已归档',
    2: '已借出',
    3: '已归还',
    4: '已销毁'
  }
  return texts[status] || '未知'
}

const fetchData = async () => {
  loading.value = true
  try {
    tableData.value = [
      {
        id: 1,
        contractName: '软件采购合同',
        contractNo: 'CT-20260427-000001',
        archiveNo: 'AR-20260427-000001',
        archiveType: 1,
        storageLocation: '电子档案库-合同类-2026',
        status: 1,
        archiveByName: '张档案',
        archiveTime: '2026-04-27 16:00:00'
      },
      {
        id: 2,
        contractName: '技术服务协议',
        contractNo: 'CT-20260426-000002',
        archiveNo: 'AR-20260426-000002',
        archiveType: 1,
        storageLocation: '电子档案库-合同类-2026',
        status: 2,
        archiveByName: '张档案',
        archiveTime: '2026-04-26 17:30:00'
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
  searchForm.contractName = ''
  searchForm.contractNo = ''
  searchForm.archiveNo = ''
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

const handleView = (row) => {
  ElMessage.info('查看归档详情')
}

const handleBorrow = (row) => {
  ElMessage.info('借阅申请')
}

onMounted(() => {
  fetchData()
})
</script>
