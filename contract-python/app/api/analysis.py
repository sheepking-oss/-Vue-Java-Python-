from fastapi import APIRouter, HTTPException
from typing import List

from app.models.contract_model import (
    ContractAnalysisRequest,
    ContractAnalysisResponse,
    KeyInfoItem,
    RiskItem,
    ExtractKeyInfoResponse,
    CheckRiskResponse
)
from app.services.key_info_extractor import KeyInfoExtractor
from app.services.risk_checker import RiskChecker

router = APIRouter()

key_info_extractor = KeyInfoExtractor()
risk_checker = RiskChecker()

@router.post("/extract", response_model=ExtractKeyInfoResponse)
async def extract_key_info(request: ContractAnalysisRequest):
    try:
        key_info = key_info_extractor.extract(request.content)
        
        return ExtractKeyInfoResponse(
            success=True,
            data=key_info
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/risk", response_model=CheckRiskResponse)
async def check_risk(request: ContractAnalysisRequest):
    try:
        risks = risk_checker.check(request.content)
        
        return CheckRiskResponse(
            success=True,
            data=risks
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/full", response_model=ContractAnalysisResponse)
async def full_analysis(request: ContractAnalysisRequest):
    try:
        key_info = key_info_extractor.extract(request.content)
        risks = risk_checker.check(request.content)
        
        return ContractAnalysisResponse(
            success=True,
            key_info=key_info,
            risks=risks,
            raw_content=request.content
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

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
    2. 如乙方逾期交付软件，每逾期一日，应向甲方支付合同金额0.1%的违约金；
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
        data=risks
    )
