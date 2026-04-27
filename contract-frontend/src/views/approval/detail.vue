<template>
  <div>
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/approval/my-approval' }">待我审批</el-breadcrumb-item>
        <el-breadcrumb-item>审批详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ approval.instanceName }}</span>
              <div>
                <el-tag :type="getStatusType(approval.status)" size="large">
                  {{ getStatusText(approval.status) }}
                </el-tag>
              </div>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="业务类型">
              {{ approval.businessType === 'contract' ? '合同审批' : approval.businessType }}
            </el-descriptions-item>
            <el-descriptions-item label="当前节点">
              {{ approval.currentNodeName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="发起人">
              {{ approval.createByName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="发起时间">
              {{ approval.createTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">
              {{ approval.startTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="结束时间">
              {{ approval.endTime || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>审批流程</span>
          </template>
          <el-steps direction="vertical" :active="currentStep" align-center>
            <el-step
              v-for="(node, index) in approvalNodes"
              :key="index"
              :title="node.nodeName"
              :description="getNodeDescription(node)"
              :status="getNodeStatus(node)"
            >
              <template #icon>
                <el-icon v-if="node.status === 2"><CircleCheckFilled /></el-icon>
                <el-icon v-else-if="node.status === 3"><CircleCloseFilled /></el-icon>
                <el-icon v-else><Clock /></el-icon>
              </template>
            </el-step>
          </el-steps>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>审批意见</span>
          </template>
          <div v-if="comments.length === 0" style="text-align: center; padding: 20px; color: #909399;">
            暂无审批意见
          </div>
          <div v-else>
            <div v-for="comment in comments" :key="comment.id" style="margin-bottom: 20px;">
              <el-divider v-if="comment !== comments[0]" />
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                  <el-avatar :size="32" icon="UserFilled" style="margin-right: 10px;" />
                  <span style="font-weight: bold;">{{ comment.approverName }}</span>
                </div>
                <el-tag :type="getCommentType(comment.actionType)" size="small">
                  {{ getCommentText(comment.actionType) }}
                </el-tag>
              </div>
              <div style="margin-top: 10px; margin-left: 42px;">
                <p style="color: #606266;">{{ comment.comment || '无意见' }}</p>
                <p style="font-size: 12px; color: #909399; margin-top: 5px;">
                  {{ comment.createTime }}
                </p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card v-if="approval.status === 1">
          <template #header>
            <span>快速审批</span>
          </template>
          <el-form :model="quickApproveForm" label-width="80px">
            <el-form-item label="操作">
              <el-radio-group v-model="quickApproveForm.actionType">
                <el-radio :value="1">通过</el-radio>
                <el-radio :value="2">驳回</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="意见">
              <el-input
                v-model="quickApproveForm.comment"
                type="textarea"
                :rows="3"
                placeholder="请输入审批意见"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" style="width: 100%;" @click="handleQuickApprove">
                提交
              </el-button>
            </el-form-item>
          </el-form>
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
              <h4>{{ log.title }}</h4>
              <p style="font-size: 12px; color: #909399;">{{ log.description }}</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getApprovalById, getApprovalNodes, getApprovalComments, processApproval } from '@/api/approval'

const route = useRoute()

const approval = ref({
  id: null,
  instanceName: '',
  businessType: 'contract',
  currentNodeName: '',
  status: 1,
  createByName: '',
  createTime: '',
  startTime: '',
  endTime: ''
})

const approvalNodes = ref([])
const comments = ref([])
const quickApproveForm = ref({
  actionType: 1,
  comment: ''
})

const operationLogs = ref([
  { title: '发起审批', description: '用户王销售发起审批', time: '2026-04-27 10:00:00' },
  { title: '部门经理审批通过', description: '张经理审核通过', time: '2026-04-27 11:00:00' },
  { title: '当前节点', description: '等待张法务审批', time: '2026-04-27 11:05:00' }
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

const getNodeStatus = (node) => {
  if (node.status === 2) return 'success'
  if (node.status === 3) return 'error'
  if (node.status === 0) return 'wait'
  return 'process'
}

const getNodeDescription = (node) => {
  if (node.actualAssigneeName) {
    return node.actualAssigneeName + (node.endTime ? ` - ${node.endTime}` : '')
  }
  return '待处理'
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

const handleQuickApprove = async () => {
  try {
    await processApproval({
      instanceId: route.params.id,
      actionType: quickApproveForm.value.actionType,
      comment: quickApproveForm.value.comment
    })
    ElMessage.success('审批成功')
    fetchData()
  } catch (error) {
    console.error('Approve error:', error)
  }
}

const fetchData = async () => {
  try {
    approval.value = {
      id: route.params.id,
      instanceName: '软件采购合同审批',
      businessType: 'contract',
      currentNodeName: '法务审核',
      status: 1,
      createByName: '王销售',
      createTime: '2026-04-27 10:00:00',
      startTime: '2026-04-27 10:00:00',
      endTime: null
    }

    approvalNodes.value = [
      { nodeName: '发起人提交', actualAssigneeName: '王销售', endTime: '2026-04-27 10:00', status: 2 },
      { nodeName: '部门经理审批', actualAssigneeName: '张经理', endTime: '2026-04-27 11:00', status: 2 },
      { nodeName: '法务审核', actualAssigneeName: null, endTime: null, status: 1 },
      { nodeName: '财务审核', actualAssigneeName: null, endTime: null, status: 0 },
      { nodeName: '总经理审批', actualAssigneeName: null, endTime: null, status: 0 }
    ]

    comments.value = [
      {
        id: 1,
        approverName: '张经理',
        actionType: 1,
        comment: '同意，符合公司采购流程。',
        createTime: '2026-04-27 11:00:00'
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
