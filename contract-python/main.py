from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import analysis

app = FastAPI(
    title="合同分析服务",
    description="企业合同关键信息抽取与风险字段提示服务",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(analysis.router, prefix="/api/analysis", tags=["合同分析"])

@app.get("/")
async def root():
    return {"message": "合同分析服务运行中", "version": "1.0.0"}

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
