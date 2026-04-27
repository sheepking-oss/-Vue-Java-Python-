<template>
  <div>
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/contract/list' }">合同列表</el-breadcrumb-item>
        <el-breadcrumb-item>{{ isEdit ? '编辑合同' : '新建合同' }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card>
      <el-form
        ref="contractForm"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 900px;"
      >
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="合同名称" prop="contractName">
          <el-input v-model="form.contractName" placeholder="请输入合同名称" />
        </el-form-item>

        <el-form-item label="合同类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择合同类型" style="width: 100%;">
            <el-option label="采购合同" :value="1" />
            <el-option label="销售合同" :value="2" />
            <el-option label="服务合同" :value="3" />
            <el-option label="租赁合同" :value="4" />
            <el-option label="合作协议" :value="5" />
          </el-select>
        </el-form-item>

        <el-form-item label="甲方" prop="partyA">
          <el-input v-model="form.partyA" placeholder="请输入甲方名称" />
        </el-form-item>

        <el-form-item label="乙方" prop="partyB">
          <el-input v-model="form.partyB" placeholder="请输入乙方名称" />
        </el-form-item>

        <el-form-item label="合同金额" prop="contractAmount">
          <el-input-number
            v-model="form.contractAmount"
            :min="0"
            :precision="2"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="币种">
          <el-select v-model="form.currency" style="width: 100%;">
            <el-option label="人民币 (CNY)" value="CNY" />
            <el-option label="美元 (USD)" value="USD" />
            <el-option label="欧元 (EUR)" value="EUR" />
          </el-select>
        </el-form-item>

        <el-form-item label="合同期限">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="签订日期">
          <el-date-picker
            v-model="form.signDate"
            type="date"
            placeholder="请选择签订日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="合同摘要">
          <el-input
            v-model="form.contractContent"
            type="textarea"
            :rows="4"
            placeholder="请输入合同内容摘要"
          />
        </el-form-item>

        <el-divider content-position="left">智能分析</el-divider>

        <el-form-item label="合同原文">
          <el-input
            v-model="analysisText"
            type="textarea"
            :rows="6"
            placeholder="请粘贴合同原文，系统将自动提取关键信息并进行风险检测"
          />
          <div style="margin-top: 10px;">
            <el-button type="primary" @click="handleAnalysis">
              <el-icon><MagicStick /></el-icon>
              智能分析
            </el-button>
          </div>
        </el-form-item>

        <div v-if="analysisResult.show" class="analysis-result">
          <div v-if="analysisResult.keyInfo.length > 0" class="section">
            <div class="section-title">
              <el-icon><InfoFilled /></el-icon>
              关键信息抽取
            </div>
            <div class="section-content">
              <el-tag
                v-for="(item, index) in analysisResult.keyInfo"
                :key="index"
                class="tag"
              >
                {{ item.label }}：{{ item.value }}
              </el-tag>
            </div>
          </div>

          <div v-if="analysisResult.risks.length > 0" class="risk-warning">
            <div class="title">
              <el-icon><WarningFilled /></el-icon>
              风险提示（{{ analysisResult.risks.length }}项）
            </div>
            <div class="item" v-for="(risk, index) in analysisResult.risks" :key="index">
              <el-icon><Warning /></el-icon>
              <span>{{ risk }}</span>
            </div>
          </div>
        </div>

        <el-divider content-position="left">变更记录</el-divider>

        <el-form-item label="变更原因" v-if="isEdit">
          <el-input
            v-model="changeReason"
            type="textarea"
            :rows="2"
            placeholder="请输入本次变更的原因，用于版本记录"
          />
        </el-form-item>

        <el-form-item>
          <div class="dialog-footer">
            <el-button @click="goBack">取消</el-button>
            <el-button type="primary" :loading="loading" @click="handleSave">
              保存草稿
            </el-button>
            <el-button type="success" :loading="loading" @click="handleSubmit">
              提交审批
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createContract, updateContract, submitApproval } from '@/api/contract'

const route = useRoute()
const router = useRouter()

const contractForm = ref(null)
const loading = ref(false)
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  contractName: '',
  typeId: null,
  partyA: '',
  partyB: '',
  contractAmount: 0,
  currency: 'CNY',
  startDate: '',
  endDate: '',
  signDate: '',
  contractContent: ''
})

const dateRange = ref([])
const changeReason = ref('')
const analysisText = ref('')

const analysisResult = reactive({
  show: false,
  keyInfo: [],
  risks: []
})

const rules = {
  contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  typeId: [{ required: true, message: '请选择合同类型', trigger: 'change' }],
  partyA: [{ required: true, message: '请输入甲方名称', trigger: 'blur' }],
  partyB: [{ required: true, message: '请输入乙方名称', trigger: 'blur' }],
  contractAmount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }]
}

watch(dateRange, (newVal) => {
  if (newVal && newVal.length === 2) {
    form.startDate = newVal[0]
    form.endDate = newVal[1]
  } else {
    form.startDate = ''
    form.endDate = ''
  }
})

const handleAnalysis = () => {
  if (!analysisText.value.trim()) {
    ElMessage.warning('请先输入合同原文')
    return
  }

  analysisResult.show = true
  analysisResult.keyInfo = [
    { label: '合同金额', value: '500,000.00元' },
    { label: '付款方式', value: '分期支付' },
    { label: '违约金比例', value: '每日0.1%' },
    { label: '争议解决', value: '仲裁' },
    { label: '管辖法院', value: '北京市海淀区人民法院' }
  ]

  analysisResult.risks = [
    '未约定合同解除的具体条件和程序',
    '违约金比例过高（每日0.1%，年化约36.5%），可能超过法定上限',
    '缺少保密条款，敏感信息存在泄露风险',
    '争议解决选择仲裁，但未明确仲裁机构名称',
    '未约定合同生效条件，存在法律风险'
  ]
}

const goBack = () => {
  router.push('/contract/list')
}

const handleSave = async () => {
  if (!contractForm.value) return
  
  await contractForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (isEdit.value) {
          await updateContract(route.params.id, form)
          ElMessage.success('保存成功')
        } else {
          await createContract(form)
          ElMessage.success('保存成功')
        }
        router.push('/contract/list')
      } catch (error) {
        console.error('Save error:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleSubmit = async () => {
  if (!contractForm.value) return
  
  await contractForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        let contractId
        if (isEdit.value) {
          await updateContract(route.params.id, form)
          contractId = route.params.id
        } else {
          const res = await createContract(form)
          contractId = res.data.id
        }
        
        await submitApproval(contractId)
        ElMessage.success('提交审批成功')
        router.push('/contract/list')
      } catch (error) {
        console.error('Submit error:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  if (isEdit.value) {
    // 模拟加载数据
    form.contractName = '软件采购合同'
    form.typeId = 1
    form.partyA = '科技有限公司'
    form.partyB = '软件供应商'
    form.contractAmount = 500000
    form.currency = 'CNY'
    form.startDate = '2026-05-01'
    form.endDate = '2027-04-30'
    form.signDate = '2026-04-25'
    form.contractContent = '关于企业管理系统软件采购的合同，包含软件授权、实施服务、技术支持等内容...'
    
    dateRange.value = [form.startDate, form.endDate]
  }
})
</script>
