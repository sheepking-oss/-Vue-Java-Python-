from pydantic import BaseModel
from typing import List, Optional

class ContractAnalysisRequest(BaseModel):
    content: str

class KeyInfoItem(BaseModel):
    label: str
    value: str
    confidence: float = 1.0

class RiskItem(BaseModel):
    level: str
    category: str
    message: str
    suggestion: Optional[str] = None

class ContractAnalysisResponse(BaseModel):
    success: bool
    key_info: List[KeyInfoItem]
    risks: List[RiskItem]
    raw_content: Optional[str] = None

class ExtractKeyInfoResponse(BaseModel):
    success: bool
    data: List[KeyInfoItem]

class CheckRiskResponse(BaseModel):
    success: bool
    data: List[RiskItem]
