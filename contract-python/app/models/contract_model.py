from pydantic import BaseModel
from typing import List, Optional

class ContractAnalysisRequest(BaseModel):
    content: str
    taskId: Optional[str] = None
    contractId: Optional[int] = None
    versionNo: Optional[int] = None

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
    taskId: Optional[str] = None
    contractId: Optional[int] = None
    versionNo: Optional[int] = None
    key_info: List[KeyInfoItem]
    risks: List[RiskItem]
    raw_content: Optional[str] = None

class ExtractKeyInfoResponse(BaseModel):
    success: bool
    taskId: Optional[str] = None
    contractId: Optional[int] = None
    versionNo: Optional[int] = None
    data: List[KeyInfoItem]

class CheckRiskResponse(BaseModel):
    success: bool
    taskId: Optional[str] = None
    contractId: Optional[int] = None
    versionNo: Optional[int] = None
    data: List[RiskItem]

class TaskValidationRequest(BaseModel):
    taskId: str
    contractId: int

class TaskValidationResponse(BaseModel):
    valid: bool
    taskId: str
    contractId: int
    message: str
