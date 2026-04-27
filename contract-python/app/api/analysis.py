from fastapi import APIRouter, HTTPException
from typing import List
import logging

from app.models.contract_model import (
    ContractAnalysisRequest,
    ContractAnalysisResponse,
    KeyInfoItem,
    RiskItem,
    ExtractKeyInfoResponse,
    CheckRiskResponse,
    TaskValidationRequest,
    TaskValidationResponse
)
from app.services.key_info_extractor import KeyInfoExtractor
from app.services.risk_checker import RiskChecker

router = APIRouter()
logger = logging.getLogger(__name__)

key_info_extractor = KeyInfoExtractor()
risk_checker = RiskChecker()

def parse_task_id_from_contract_id(task_id: str) -> int:
    """从任务ID中提取合同ID，用于本地校验"""
    try:
        if task_id and task_id.startswith("CT-"):
            parts = task_id.split("-")
            if len(parts) >= 2:
                return int(parts[1])
    except (ValueError, IndexError) as e:
        logger.warning(f"Failed to parse contractId from taskId: {task_id}, error: {e}")
    return -1

def validate_task_locally(request: ContractAnalysisRequest) -> bool:
    """
    本地校验任务绑定关系
    任务ID格式: CT-{contractId}-V{versionNo}-{random}
    例如: CT-123-V1-ABC123DEF456
    """
    if request.taskId is None or request.contractId is None:
        return True
    
    parsed_contract_id = parse_task_id_from_contract_id(request.taskId)
    
    if parsed_contract_id == -1:
        logger.warning(f"Could not parse contractId from taskId: {request.taskId}")
        return True
    
    if parsed_contract_id != request.contractId:
        logger.error(f"Task binding validation failed! taskId={request.taskId}, "
                    f"parsedContractId={parsed_contract_id}, requestContractId={request.contractId}")
        return False
    
    logger.info(f"Task binding validation passed: taskId={request.taskId}, contractId={request.contractId}")
    return True

@router.post("/extract", response_model=ExtractKeyInfoResponse)
async def extract_key_info(request: ContractAnalysisRequest):
    try:
        logger.info(f"Received extract request: taskId={request.taskId}, contractId={request.contractId}")
        
        if not validate_task_locally(request):
            raise HTTPException(
                status_code=400, 
                detail=f"任务绑定关系校验失败: taskId={request.taskId}, contractId={request.contractId}"
            )
        
        key_info = key_info_extractor.extract(request.content)
        
        response = ExtractKeyInfoResponse(
            success=True,
            taskId=request.taskId,
            contractId=request.contractId,
            versionNo=request.versionNo,
            data=key_info
        )
        
        logger.info(f"Extract completed: taskId={request.taskId}, keyInfoCount={len(key_info)}")
        return response
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Extract failed: taskId={request.taskId}, error={str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/risk", response_model=CheckRiskResponse)
async def check_risk(request: ContractAnalysisRequest):
    try:
        logger.info(f"Received risk check request: taskId={request.taskId}, contractId={request.contractId}")
        
        if not validate_task_locally(request):
            raise HTTPException(
                status_code=400, 
                detail=f"任务绑定关系校验失败: taskId={request.taskId}, contractId={request.contractId}"
            )
        
        risks = risk_checker.check(request.content)
        
        response = CheckRiskResponse(
            success=True,
            taskId=request.taskId,
            contractId=request.contractId,
            versionNo=request.versionNo,
            data=risks
        )
        
        logger.info(f"Risk check completed: taskId={request.taskId}, riskCount={len(risks)}")
        return response
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Risk check failed: taskId={request.taskId}, error={str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/full", response_model=ContractAnalysisResponse)
async def full_analysis(request: ContractAnalysisRequest):
    try:
        logger.info(f"Received full analysis request: taskId={request.taskId}, contractId={request.contractId}")
        
        if not validate_task_locally(request):
            raise HTTPException(
                status_code=400, 
                detail=f"任务绑定关系校验失败: taskId={request.taskId}, contractId={request.contractId}"
            )
        
        key_info = key_info_extractor.extract(request.content)
        risks = risk_checker.check(request.content)
        
        response = ContractAnalysisResponse(
            success=True,
            taskId=request.taskId,
            contractId=request.contractId,
            versionNo=request.versionNo,
            key_info=key_info,
            risks=risks,
            raw_content=request.content
        )
        
        logger.info(f"Full analysis completed: taskId={request.taskId}, "
                   f"keyInfoCount={len(key_info)}, riskCount={len(risks)}")
        return response
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Full analysis failed: taskId={request.taskId}, error={str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/validate", response_model=TaskValidationResponse)
async def validate_task(request: TaskValidationRequest):
    """
    任务绑定关系校验端点
    用于 Java 后端主动校验任务与合同的绑定关系
    """
    logger.info(f"Received validation request: taskId={request.taskId}, contractId={request.contractId}")
    
    parsed_contract_id = parse_task_id_from_contract_id(request.taskId)
    
    if parsed_contract_id == -1:
        return TaskValidationResponse(
            valid=False,
            taskId=request.taskId,
            contractId=request.contractId,
            message=f"无法从任务ID中解析合同ID: {request.taskId}"
        )
    
    if parsed_contract_id != request.contractId:
        return TaskValidationResponse(
            valid=False,
            taskId=request.taskId,
            contractId=request.contractId,
            message=f"合同ID不匹配: 任务ID中的合同ID={parsed_contract_id}, 请求的合同ID={request.contractId}"
        )
    
    return TaskValidationResponse(
        valid=True,
        taskId=request.taskId,
        contractId=request.contractId,
        message="校验通过"
    )

@router.get("/demo/extract", response_model=ExtractKeyInfoResponse)
async def demo_extract():
    demo_content = """
    软件采购合同

    甲方：科技有限公司
    乙方：软件供应商有限公司

    鉴于甲方需要购买企业管理系统软件，乙方具备提供该软件的资质和能力，经双方友好协商，达成如下协议：

    第一条 合同标的
    甲方同意购买乙方开发的企业管理系统软件V2.0版本，乙方同意向甲方提供该软件及相关服务。

    第二条 合同金额
    本合同总金额为人民币500,000元整（大写：伍拾万元整）。
    付款方式：
    1. 合同签订后3个工作日内，甲方向乙方支付合同金额的30%，即150,000元；
    2. 软件安装部署完成并验收合格后3个工作日内，甲方向乙方支付合同金额的60%，即300,000元；
    3. 质保期满后3个工作日内，甲方向乙方支付合同金额的10%，即50,000元。

    第三条 合同期限
    本合同自2026年5月1日起生效，至2027年4月30日止。
    质保期为自验收合格之日起12个月。

    第四条 违约责任
    1. 如甲方逾期付款，每逾期一日，应向乙方支付逾期金额0.1%的违约金；
    2. 如乙方逾期交付软件，每逾期一日，应向乙方支付合同金额0.1%的违约金；
    3. 如乙方交付的软件不符合合同约定，甲方有权解除合同，并要求乙方退还已收款项。

    第五条 争议解决
    本合同履行过程中发生的争议，双方应友好协商解决；协商不成的，任何一方均可向有管辖权的人民法院提起诉讼。

    第六条 其他
    本合同未尽事宜，由双方另行签订补充协议。
    本合同一式两份，甲乙双方各执一份，具有同等法律效力。

    甲方（盖章）：科技有限公司
    法定代表人：张三
    签订日期：2026年4月27日

    乙方（盖章）：软件供应商有限公司
    法定代表人：李四
    签订日期：2026年4月27日
    """

    key_info = key_info_extractor.extract(demo_content)
    
    return ExtractKeyInfoResponse(
        success=True,
        taskId="CT-DEMO-V1-001",
        contractId=1,
        versionNo=1,
        data=key_info
    )

@router.get("/demo/risk", response_model=CheckRiskResponse)
async def demo_risk():
    demo_content = """
    软件采购合同

    甲方：科技有限公司
    乙方：软件供应商有限公司

    第一条 合同金额
    本合同总金额为人民币500,000元整。
    付款方式：合同签订后一次性付清。

    第二条 合同期限
    本合同自签订之日起生效，有效期一年。

    第三条 违约责任
    1. 如甲方逾期付款，每逾期一日，应向乙方支付逾期金额0.15%的违约金；
    2. 乙方对软件质量不承担任何责任。

    第四条 争议解决
    双方发生争议时，应协商解决。协商不成的，通过仲裁解决。

    第五条 其他
    本合同未尽事宜，双方另行协商。
    本合同不得解除，不得终止。
    """

    risks = risk_checker.check(demo_content)
    
    return CheckRiskResponse(
        success=True,
        taskId="CT-DEMO-V1-001",
        contractId=1,
        versionNo=1,
        data=risks
    )
