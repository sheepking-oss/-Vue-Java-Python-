<template>
  <div>
    <div class="page-header">
      <span class="title">合同统计</span>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <div class="stat-card">
          <div class="title">合同总数</div>
          <div class="value text-primary">{{ stats.total }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="title">审批中</div>
          <div class="value text-warning">{{ stats.approving }}</div>
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
          <div class="title">已归档</div>
          <div class="value text-primary">{{ stats.archived }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>按合同类型统计</span>
          </template>
          <div ref="typeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>按状态统计</span>
          </template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>月度趋势</span>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                size="small"
                @change="handleDateChange"
              />
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>按部门统计</span>
          </template>
          <div ref="deptChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getContractStatsByType, getContractStatsByStatus, getContractStatsByMonth, getContractStatsByDept } from '@/api/report'

const stats = reactive({
  total: 28,
  approving: 5,
  passed: 18,
  archived: 12
})

const dateRange = ref([])

const typeChartRef = ref(null)
const statusChartRef = ref(null)
const trendChartRef = ref(null)
const deptChartRef = ref(null)

const initTypeChart = () => {
  const chart = echarts.init(typeChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '合同类型',
        type: 'pie',
        radius: '60%',
        data: [
          { value: 10, name: '采购合同' },
          { value: 8, name: '销售合同' },
          { value: 5, name: '服务合同' },
          { value: 3, name: '租赁合同' },
          { value: 2, name: '合作协议' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  chart.setOption(option)
}

const initStatusChart = () => {
  const chart = echarts.init(statusChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: '5%',
      left: 'center'
    },
    series: [
      {
        name: '合同状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 3, name: '草稿', itemStyle: { color: '#909399' } },
          { value: 5, name: '审批中', itemStyle: { color: '#e6a23c' } },
          { value: 12, name: '已通过', itemStyle: { color: '#67c23a' } },
          { value: 2, name: '已驳回', itemStyle: { color: '#f56c6c' } },
          { value: 6, name: '已归档', itemStyle: { color: '#409eff' } }
        ]
      }
    ]
  }
  chart.setOption(option)
}

const initTrendChart = () => {
  const chart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['合同数量', '合同金额']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: [
      {
        type: 'value',
        name: '数量'
      },
      {
        type: 'value',
        name: '金额(万)',
        position: 'right'
      }
    ],
    series: [
      {
        name: '合同数量',
        type: 'bar',
        data: [3, 5, 4, 6, 5, 3]
      },
      {
        name: '合同金额',
        type: 'line',
        yAxisIndex: 1,
        data: [150, 280, 220, 350, 290, 180]
      }
    ]
  }
  chart.setOption(option)
}

const initDeptChart = () => {
  const chart = echarts.init(deptChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: ['法务部', '财务部', '业务部', '行政部', '销售一部', '销售二部']
    },
    series: [
      {
        name: '合同数量',
        type: 'bar',
        data: [8, 6, 5, 3, 4, 2],
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
  chart.setOption(option)
}

const handleDateChange = () => {
  // 重新加载趋势数据
}

onMounted(() => {
  nextTick(() => {
    initTypeChart()
    initStatusChart()
    initTrendChart()
    initDeptChart()
  })
})
</script>
