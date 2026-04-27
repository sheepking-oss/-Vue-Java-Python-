-- =============================================
-- 合同分析异步任务机制优化脚本
-- 功能：为每份合同生成全局唯一任务标识，与合同主键强绑定
-- 原则：风险识别、文本抽取结果回调时严格校验唯一标识，确保并发处理不串单、不错绑
-- =============================================

USE contract_platform;

-- =============================================
-- 1. 新增：合同分析任务表
-- 作用：存储分析任务ID与合同的绑定关系，支持结果校验和追踪
-- =============================================
CREATE TABLE IF NOT EXISTS contract_analysis_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id VARCHAR(64) NOT NULL COMMENT '全局唯一任务标识',
    contract_id BIGINT NOT NULL COMMENT '合同ID（强绑定）',
    version_no INT COMMENT '合同版本号',
    analysis_type VARCHAR(20) NOT NULL COMMENT '分析类型(KEY_INFO关键信息抽取,RISK风险检测,FULL完整分析)',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态(PENDING等待中,PROCESSING处理中,SUCCESS成功,FAILED失败)',
    content_hash VARCHAR(64) COMMENT '内容哈希(MD5)，用于校验内容一致性',
    result TEXT COMMENT '分析结果(JSON格式)',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    start_time DATETIME COMMENT '开始处理时间',
    end_time DATETIME COMMENT '结束时间',
    create_by BIGINT COMMENT '创建人ID',
    UNIQUE KEY uk_task_id (task_id),
    INDEX idx_contract_id (contract_id),
    INDEX idx_contract_version (contract_id, version_no),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同分析任务表';

-- =============================================
-- 2. 任务ID格式说明
-- =============================================
-- 任务ID格式: CT-{contractId}-V{versionNo}-{random12char}
-- 示例: CT-123-V1-ABC123DEF456
-- 
-- 组成部分:
-- - CT: 前缀，表示Contract Task
-- - {contractId}: 合同主键ID（强绑定）
-- - V{versionNo}: 版本号标识（可选，默认V0）
-- - {random12char}: 12位随机字符，确保全局唯一
--
-- 优势:
-- 1. 从任务ID可直接解析出合同ID，无需数据库查询即可进行初步校验
-- 2. 包含版本号，支持同一合同不同版本的并行分析
-- 3. 随机字符确保全局唯一性，避免冲突

-- =============================================
-- 3. 三重校验机制
-- =============================================
-- 校验层级:
--
-- 第一层：任务ID格式校验 (Python服务本地)
-- - 检查任务ID是否符合 CT-{contractId}-V{versionNo}-{random} 格式
-- - 从任务ID中解析出合同ID
--
-- 第二层：请求参数校验 (Python服务本地)
-- - 检查请求中的 contractId 与任务ID解析出的合同ID是否一致
-- - 不一致则直接拒绝，防止串单
--
-- 第三层：数据库校验 (Java服务)
-- - 查询 contract_analysis_task 表，验证 taskId 与 contractId 的绑定关系
-- - 双重保险，确保数据一致性
--
-- 校验流程图:
-- ┌─────────────────┐
-- │  前端发起请求    │
-- │  (contractId)   │
-- └────────┬────────┘
--          │
--          ▼
-- ┌─────────────────┐
-- │  Java后端       │
-- │  生成任务ID     │
-- │  存入数据库     │
-- └────────┬────────┘
--          │
--          ▼
-- ┌─────────────────┐
-- │  调用Python服务 │
-- │  taskId +       │
-- │  contractId +   │
-- │  versionNo +    │
-- │  content        │
-- └────────┬────────┘
--          │
--          ▼
-- ┌─────────────────┐
-- │  Python服务     │
-- │  第一层校验:    │
-- │  任务ID格式     │
-- └────────┬────────┘
--          │
--          ▼
-- ┌─────────────────┐
-- │  Python服务     │
-- │  第二层校验:    │
-- │  contractId匹配 │
-- └────────┬────────┘
--          │
--          ▼
-- ┌─────────────────┐
-- │  执行分析        │
-- │  返回结果       │
-- └────────┬────────┘
--          │
--          ▼
-- ┌─────────────────┐
-- │  Java后端       │
-- │  第三层校验:    │
-- │  数据库绑定关系 │
-- └────────┬────────┘
--          │
--          ▼
-- ┌─────────────────┐
-- │  存储结果       │
-- │  返回前端       │
-- └─────────────────┘

-- =============================================
-- 4. 并发安全保障
-- =============================================
-- 场景：合同A(Id=100)和合同B(Id=200)同时发起分析请求
--
-- 传统方式风险:
-- - 若使用简单的UUID，无法从ID本身判断归属
-- - 并发情况下可能出现回调数据错绑
--
-- 优化后方式:
-- - 合同A的任务ID: CT-100-V1-ABC123DEF456
-- - 合同B的任务ID: CT-200-V1-XYZ789GHI012
--
-- 安全保障:
-- 1. 唯一键约束: uk_task_id 确保任务ID全局唯一
-- 2. 本地校验: Python服务在处理前验证合同ID匹配
-- 3. 数据库校验: Java服务在接收结果后再次验证绑定关系
-- 4. 可追溯: 所有任务记录可查询，支持问题排查

-- =============================================
-- 5. 插入示例数据（用于测试分析任务机制）
-- =============================================

-- 场景：合同ID=1，版本1，发起完整分析
-- INSERT INTO contract_analysis_task 
-- (task_id, contract_id, version_no, analysis_type, status, content_hash, create_by)
-- VALUES 
-- ('CT-1-V1-A1B2C3D4E5F6', 1, 1, 'FULL', 'SUCCESS', 'd41d8cd98f00b204e9800998ecf8427e', 1);

-- 场景：合同ID=1，版本2，发起风险检测
-- INSERT INTO contract_analysis_task 
-- (task_id, contract_id, version_no, analysis_type, status, content_hash, create_by)
-- VALUES 
-- ('CT-1-V2-X9Y8Z7W6V5U4', 1, 2, 'RISK', 'PENDING', 'd41d8cd98f00b204e9800998ecf8427e', 1);

-- 场景：合同ID=2，版本1，发起关键信息抽取
-- INSERT INTO contract_analysis_task 
-- (task_id, contract_id, version_no, analysis_type, status, content_hash, create_by)
-- VALUES 
-- ('CT-2-V1-P0Q1R2S3T4U5', 2, 1, 'KEY_INFO', 'PROCESSING', 'd41d8cd98f00b204e9800998ecf8427e', 4);

-- =============================================
-- 6. 查询示例
-- =============================================

-- 查询某个合同的所有分析任务
-- SELECT * FROM contract_analysis_task 
-- WHERE contract_id = 1 
-- ORDER BY create_time DESC;

-- 查询某个合同特定版本的分析任务
-- SELECT * FROM contract_analysis_task 
-- WHERE contract_id = 1 AND version_no = 2;

-- 查询所有失败的任务
-- SELECT * FROM contract_analysis_task 
-- WHERE status = 'FAILED';

-- 统计各分析类型的成功率
-- SELECT 
--     analysis_type,
--     COUNT(*) as total,
--     SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count,
--     ROUND(SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as success_rate
-- FROM contract_analysis_task
-- GROUP BY analysis_type;

-- =============================================
-- 7. 权限说明
-- =============================================
-- 新增权限:
-- - contract:analysis - 合同分析权限
--
-- 建议在 sys_permission 表中添加:
-- INSERT INTO sys_permission (parent_id, menu_name, menu_type, path, component, perms, visible, sort, status, del_flag)
-- VALUES 
-- (0, '合同分析', 'F', NULL, NULL, 'contract:analysis', 1, 1, 1, 0);
