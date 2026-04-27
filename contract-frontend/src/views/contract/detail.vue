<template>
  <div>
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/contract/list' }">合同列表</el-breadcrumb-item>
        <el-breadcrumb-item>合同详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>合同信息</span>
              <div>
                <el-button
                  v-if="contract.status === 0 || contract.status === 4"
                  type="primary"
                  @click="goToEdit"
                >
                  编辑
                </el-button>
                <el-button
                  v-if="contract.status === 0 || contract.status === 4"
                  type="success"
                  @click="handleSubmit"
                >
                  提交审批
                </el-button>
                <el-button
                  v-if="contract.status === 2"
                  type="warning"
                  @click="handleArchive"
                >
                  归档
                </el-button>
              </div>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="合同名称" :span="2">
              {{ contract.contractName }}
            </el-descriptions-item>
            <el-descriptions-item label="合同编号">
              {{ contract.contractNo }}
            </el-descriptions-item>
            <el-descriptions-item label="合同类型">
              <el-tag size="small">{{ contract.typeName }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="甲方">
              {{ contract.partyA }}
            </el-descriptions-item>
            <el-descriptions-item label="乙方">
              {{ contract.partyB }}
            </el-descriptions-item>
            <el-descriptions-item label="合同金额">
              <span style="color: #f56c6c; font-weight: bold;">
                {{ formatAmount(contract.contractAmount) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="币种">
              {{ contract.currency }}
            </el-descriptions-item>
            <el-descriptions-item label="合同期限">
              {{ contract.startDate }} 至 {{ contract.endDate }}
            </el-descriptions-item>
            <el-descriptions-item label="签订日期">
              {{ contract.signDate || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusType(contract.status)" size="small">
                {{ getStatusText(contract.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前版本">
              V{{ contract.currentVersion }}
            </el-descriptions-item>
            <el-descriptions-item label="合同摘要" :span="2">
              {{ contract.contractContent || '暂无摘要' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>版本历史</span>
          </template>
          <div v-if="versions.length === 0" style="text-align: center; padding: 20px; color: #909399;">
            暂无版本记录
          </div>
          <div v-else>
            <div v-for="version in versions" :key="version.id" class="version-item">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                  <span style="font-weight: bold;">版本 V{{ version.versionNo }}</span>
                  <el-tag v-if="version.versionNo === contract.currentVersion" type="success" size="small" style="margin-left: 10px;">
                    当前版本
                  </el-tag>
                </div>
                <el-button type="primary" link size="small">查看详情</el-button>
              </div>
              <div style="margin-top: 10px; font-size: 12px; color: #909399;">
                <span>创建人：{{ version.createByName || '未知' }}</span>
                <span style="margin-left: 20px;">创建时间：{{ version.createTime }}</span>
              </div>
              <div v-if="version.changeReason" style="margin-top: 5px; font-size: 13px; color: #606266;">
                变更原因：{{ version.changeReason }}
              </div>
            </div>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>附件列表</span>
          </template>
          <div v-if="attachments.length === 0" style="text-align: center; padding: 20px; color: #909399;">
            暂无附件
          </div>
          <div v-else>
            <div v-for="attachment in attachments" :key="attachment.id" class="attachment-item">
              <el-icon class="icon"><Document /></el-icon>
              <div class="info">
                <div class="name">{{ attachment.fileName }}</div>
                <div class="size">
                  {{ formatFileSize(attachment.fileSize) }} | 上传时间：{{ attachment.createTime }}
                </div>
              </div>
              <div>
                <el-button type="primary" link size="small">下载</el-button>
                <el-button type="danger" link size="small">删除</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card v-if="approvalInstance">
          <template #header>
            <span>审批进度</span>
          </template>
          <el-steps direction="vertical" :active="currentStep">
            <el-step
              v-for="(node, index) in approvalNodes"
              :key="index"
              :title="node.nodeName"
              :description="node.actualAssigneeName || '待处理'"
              :status="getNodeStatus(node)"
            />
          </el-steps>
        </el-card>

        <el-card v-if="approvalComments.length > 0" style="margin-top: 20px;">
          <template #header>
            <span>审批意见</span>
          </template>
          <div v-for="comment in approvalComments" :key="comment.id" style="margin-bottom: 15px;">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-weight: bold;">{{ comment.approverName }}</span>
              <el-tag :type="getCommentType(comment.actionType)" size="small">
                {{ getCommentText(comment.actionType) }}
              </el-tag>
            </div>
            <div style="margin-top: 5px; font-size: 13px; color: #606266;">
              {{ comment.comment || '无意见' }}
            </div>
            <div style="margin-top: 5px; font-size: 12px; color: #909399;">
              {{ comment.createTime }}
            </div>
            <el-divider v-if="comment !== approvalComments[approvalComments.length - 1]" />
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>操作记录</span>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(log, index) in operationLogs"
              :key="index"
              :timestamp="log.time"
              placement="top"
            >
              <el-card>
                <h4>{{ log.title }}</h4>
                <p style="font-size: 12px; color: #909399;">{{ log.description }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getContractById, getVersions, getAttachments, submitApproval, archiveContract } from '@/api/contract'
import { getApprovalByBusiness, getApprovalNodes, getApprovalComments } from '@/api/approval'

const route = useRoute()
const router = useRouter()

const contract = ref({
  id: null,
  contractName: '',
  contractNo: '',
  typeName: '',
  partyA: '',
  partyB: '',
  contractAmount: 0,
  currency: 'CNY',
  startDate: '',
  endDate: '',
  signDate: '',
  contractContent: '',
  currentVersion: 1,
  status: 0
})

const versions = ref([])
const attachments = ref([])
const approvalInstance = ref(null)
const approvalNodes = ref([])
const approvalComments = ref([])

const operationLogs = ref([
  { title: '合同创建', description: '用户王销售创建了合同', time: '2026-04-27 10:00:00' },
  { title: '提交审批', description: '合同提交审批，当前节点：法务审核', time: '2026-04-27 10:30:00' },
  { title: '法务审核通过', description: '张法务审核通过', time: '2026-04-27 14:00:00' },
  { title: '财务审核中', description: '当前节点：财务审核', time: '2026-04-27 14:15:00' }
])

const currentStep = computed(() => {
  const passedCount = approvalNodes.value.filter(n => 
    n.status === 2 || n.status === 3
  ).length
  return passedCount
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

const getNodeStatus = (node) => {
  if (node.status === 2) return 'success'
  if (node.status === 3) return 'error'
  if (node.status === 0) return 'wait'
  return 'process'
}

const getCommentType = (actionType) => {
  const types = {
    1: 'success',
    2: 'danger',
    3: 'warning',
    4: 'info',
    5: ''
  }
  return types[actionType] || ''
}

const getCommentText = (actionType) => {
  const texts = {
    1: '通过',
    2: '驳回',
    3: '退回',
    4: '撤回',
    5: '抄送'
  }
  return texts[actionType] || '未知'
}

const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '¥0.00'
  return '¥' + Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

const goToEdit = () => {
  router.push(`/contract/edit/${contract.value.id}`)
}

const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm(`确定要提交合同"${contract.value.contractName}"进行审批吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await submitApproval(contract.value.id)
    ElMessage.success('提交审批成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Submit error:', error)
    }
  }
}

const handleArchive = async () => {
  try {
    await ElMessageBox.confirm(`确定要归档合同"${contract.value.contractName}"吗？归档后将无法修改。`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await archiveContract(contract.value.id)
    ElMessage.success('归档成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Archive error:', error)
    }
  }
}

const fetchData = async () => {
  try {
    // 模拟数据
    contract.value = {
      id: route.params.id,
      contractName: '软件采购合同',
      contractNo: 'CT-20260427-000001',
      typeName: '采购合同',
      partyA: '科技有限公司',
      partyB: '软件供应商',
      contractAmount: 500000,
      currency: 'CNY',
      startDate: '2026-05-01',
      endDate: '2027-04-30',
      signDate: '2026-04-25',
      contractContent: '关于企业管理系统软件采购的合同，包含软件授权、实施服务、技术支持等内容。',
      currentVersion: 1,
      status: 2
    }

    versions.value = [
      {
        id: 1,
        versionNo: 1,
        createByName: '王销售',
        createTime: '2026-04-27 10:00:00',
        changeReason: '初始版本'
      }
    ]

    attachments.value = [
      {
        id: 1,
        fileName: '软件采购合同.pdf',
        fileSize: 1024000,
        createTime: '2026-04-27 10:05:00'
      }
    ]

    approvalInstance.value = {
      id: 1,
      instanceName: '软件采购合同审批',
      status: 2,
      currentNodeName: '审批完成'
    }

    approvalNodes.value = [
      { nodeName: '发起人提交', actualAssigneeName: '王销售', status: 2 },
      { nodeName: '部门经理审批', actualAssigneeName: '张经理', status: 2 },
      { nodeName: '法务审核', actualAssigneeName: '张法务', status: 2 },
      { nodeName: '财务审核', actualAssigneeName: '李财务', status: 2 },
      { nodeName: '总经理审批', actualAssigneeName: '王总', status: 2 }
    ]

    approvalComments.value = [
      {
        id: 1,
        approverName: '张经理',
        actionType: 1,
        comment: '同意，符合公司采购流程。',
        createTime: '2026-04-27 11:00:00'
      },
      {
        id: 2,
        approverName: '张法务',
        actionType: 1,
        comment: '合同条款合法，无法律风险。',
        createTime: '2026-04-27 14:00:00'
      },
      {
        id: 3,
        approverName: '李财务',
        actionType: 1,
        comment: '预算充足，付款条款合理。',
        createTime: '2026-04-27 15:30:00'
      }
    ]
  } catch (error) {
    console.error('Fetch error:', error)
  }
}

onMounted(() => {
  fetchData()
})
</script>
