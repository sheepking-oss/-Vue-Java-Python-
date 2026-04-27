<template>
  <div>
    <div class="page-header">
      <span class="title">审批统计</span>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <div class="stat-card">
          <div class="title">发起审批</div>
          <div class="value text-primary">{{ stats.initiated }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="title">待我审批</div>
          <div class="value text-warning">{{ stats.pending }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="title">已通过</div>
          <div class="value text-success">{{ stats.passed }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="title">已驳回</div>
          <div class="value text-danger">{{ stats.rejected }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>审批效率分析</span>
          </template>
          <div ref="efficiencyChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>各节点通过率</span>
          </template>
          <div ref="nodeRateChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>审批明细</span>
          </template>
          <el-table :data="detailList" style="width: 100%">
            <el-table-column prop="instanceName" label="审批名称" min-width="250" />
            <el-table-column prop="businessType" label="业务类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">
                  {{ row.businessType === 'contract' ? '合同审批' : row.businessType }}
                </el-tag>
              </template>
            </el-table-column>
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
            <el-table-column prop="duration" label="耗时" width="120">
              <template #default="{ row }">
                {{ row.duration || '-' }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const stats = reactive({
  initiated: 35,
  pending: 5,
  passed: 25,
  rejected: 3
})

const efficiencyChartRef = ref(null)
const nodeRateChartRef = ref(null)

const detailList = ref([
  {
    instanceName: '软件采购合同审批',
    businessType: 'contract',
    status: 2,
    startTime: '2026-04-27 10:00:00',
    endTime: '2026-04-27 16:30:00',
    duration: '6小时30分'
  },
  {
    instanceName: '设备采购合同审批',
    businessType: 'contract',
    status: 1,
    startTime: '2026-04-26 15:30:00',
    endTime: null,
    duration: null
  },
  {
    instanceName: '办公设备租赁合同审批',
    businessType: 'contract',
    status: 3,
    startTime: '2026-04-25 11:00:00',
    endTime: '2026-04-25 16:30:00',
    duration: '5小时30分'
  }
])

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

const initEfficiencyChart = () => {
  const chart = echarts.init(efficiencyChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['平均耗时(分钟)']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['发起人提交', '部门经理审批', '法务审核', '财务审核', '总经理审批']
    },
    yAxis: {
      type: 'value',
      name: '分钟'
    },
    series: [
      {
        name: '平均耗时(分钟)',
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.3
        },
        data: [5, 45, 120, 90, 60],
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
  chart.setOption(option)
}

const initNodeRateChart = () => {
  const chart = echarts.init(nodeRateChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['通过率', '驳回率']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['部门经理审批', '法务审核', '财务审核', '总经理审批']
    },
    yAxis: {
      type: 'value',
      name: '百分比',
      max: 100
    },
    series: [
      {
        name: '通过率',
        type: 'bar',
        stack: 'total',
        data: [95, 88, 92, 98],
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '驳回率',
        type: 'bar',
        stack: 'total',
        data: [5, 12, 8, 2],
        itemStyle: {
          color: '#f56c6c'
        }
      }
    ]
  }
  chart.setOption(option)
}

onMounted(() => {
  nextTick(() => {
    initEfficiencyChart()
    initNodeRateChart()
  })
})
</script>
