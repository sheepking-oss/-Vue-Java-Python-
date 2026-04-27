-- =============================================
-- 合同版本管理与审批流绑定优化脚本
-- 功能：建立版本号唯一标识 + 流程实例强关联的架构
-- 原则：每一次退回修改都生成独立新版本，正文、附件、审批意见、流程节点全部与版本号绑定
-- =============================================

USE contract_platform;

-- =============================================
-- 1. 新增：合同版本与审批实例关联表
-- 作用：建立版本号与审批实例的强关联
-- =============================================
CREATE TABLE IF NOT EXISTS contract_version_approval (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_id BIGINT NOT NULL COMMENT '合同ID',
    version_no INT NOT NULL COMMENT '版本号',
    instance_id BIGINT NOT NULL COMMENT '审批实例ID',
    is_current TINYINT DEFAULT 0 COMMENT '是否当前版本审批(0否 1是)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_contract_version (contract_id, version_no),
    UNIQUE KEY uk_instance (instance_id),
    INDEX idx_contract_id (contract_id),
    INDEX idx_version_no (contract_id, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同版本与审批实例关联表';

-- =============================================
-- 2. 修改：合同附件表增加版本号字段
-- 作用：附件与具体版本绑定，防止新旧版本附件混用
-- =============================================
ALTER TABLE contract_attachment 
ADD COLUMN version_no INT COMMENT '版本号' AFTER version_id;

-- =============================================
-- 3. 修改：审批实例表增加合同版本号字段
-- 作用：审批实例与合同版本强绑定
-- =============================================
ALTER TABLE approval_instance 
ADD COLUMN contract_version_no INT COMMENT '合同版本号' AFTER business_type;

-- =============================================
-- 4. 修改：审批实例节点表增加版本号字段
-- 作用：节点数据与版本绑定
-- =============================================
ALTER TABLE approval_instance_node 
ADD COLUMN contract_version_no INT COMMENT '合同版本号' AFTER node_id;

-- =============================================
-- 5. 修改：审批意见表增加版本号字段
-- 作用：审批意见与版本绑定
-- =============================================
ALTER TABLE approval_comment 
ADD COLUMN contract_version_no INT COMMENT '合同版本号' AFTER node_id;

-- =============================================
-- 6. 新增：合同版本变更记录表
-- 作用：记录版本变更历史，支持版本对比
-- =============================================
CREATE TABLE IF NOT EXISTS contract_version_change (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_id BIGINT NOT NULL COMMENT '合同ID',
    from_version_no INT NOT NULL COMMENT '原版本号',
    to_version_no INT NOT NULL COMMENT '新版本号',
    change_type VARCHAR(50) NOT NULL COMMENT '变更类型(CREATE创建,UPDATE更新,RETURN退回修改,RETRY重新提交)',
    change_reason VARCHAR(500) COMMENT '变更原因',
    changed_fields VARCHAR(1000) COMMENT '变更字段(JSON格式)',
    create_by BIGINT COMMENT '操作人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_contract_id (contract_id),
    INDEX idx_from_version (contract_id, from_version_no),
    INDEX idx_to_version (contract_id, to_version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同版本变更记录表';

-- =============================================
-- 7. 修改：合同主表增加当前审批版本号字段
-- 作用：明确当前正在审批的版本
-- =============================================
ALTER TABLE contract 
ADD COLUMN approval_version_no INT COMMENT '当前审批版本号' AFTER current_version;

-- =============================================
-- 8. 创建触发器：版本号自动递增
-- 说明：确保每个合同的版本号唯一且连续
-- =============================================
DELIMITER //
CREATE TRIGGER trg_contract_version_before_insert
BEFORE INSERT ON contract_version
FOR EACH ROW
BEGIN
    DECLARE max_version INT;
    
    SELECT MAX(version_no) INTO max_version 
    FROM contract_version 
    WHERE contract_id = NEW.contract_id;
    
    IF max_version IS NULL THEN
        SET NEW.version_no = 1;
    ELSE
        SET NEW.version_no = max_version + 1;
    END IF;
END //
DELIMITER ;

-- =============================================
-- 9. 数据迁移：为现有附件和审批实例增加默认版本号
-- =============================================
-- 为现有附件设置默认版本号（取当前版本）
UPDATE contract_attachment ca
JOIN contract c ON ca.contract_id = c.id
SET ca.version_no = c.current_version
WHERE ca.version_no IS NULL;

-- 为现有审批实例设置默认版本号（取当前版本）
UPDATE approval_instance ai
JOIN contract c ON ai.business_id = c.id AND ai.business_type = 'contract'
SET ai.contract_version_no = c.current_version
WHERE ai.contract_version_no IS NULL;

-- =============================================
-- 10. 新增索引优化查询性能
-- =============================================
-- 合同版本表索引优化
CREATE INDEX idx_contract_version_contract ON contract_version(contract_id);
CREATE INDEX idx_contract_version_version ON contract_version(contract_id, version_no);

-- 合同附件表索引优化
CREATE INDEX idx_attachment_contract ON contract_attachment(contract_id);
CREATE INDEX idx_attachment_version ON contract_attachment(contract_id, version_no);

-- 审批实例表索引优化
CREATE INDEX idx_approval_business ON approval_instance(business_id, business_type);
CREATE INDEX idx_approval_contract_version ON approval_instance(business_id, contract_version_no);

-- =============================================
-- 11. 插入示例数据（用于测试版本与审批流绑定）
-- =============================================
-- 场景：合同 V1 提交审批 → 被退回 → 创建 V2 → 重新提交审批

-- 1. 假设合同 ID=1 已存在，当前版本=1

-- 2. 提交 V1 审批，创建关联
-- INSERT INTO contract_version_approval (contract_id, version_no, instance_id, is_current)
-- VALUES (1, 1, 100, 1);

-- 3. 审批被退回，解除当前版本审批关联
-- UPDATE contract_version_approval SET is_current = 0 WHERE contract_id = 1 AND version_no = 1;

-- 4. 修改合同，创建 V2 版本
-- 版本号由触发器自动递增为 2

-- 5. 记录版本变更
-- INSERT INTO contract_version_change (contract_id, from_version_no, to_version_no, change_type, change_reason)
-- VALUES (1, 1, 2, 'RETURN', '法务审核不通过，需要修改付款条款');

-- 6. 提交 V2 审批，创建新的审批实例和关联
-- INSERT INTO contract_version_approval (contract_id, version_no, instance_id, is_current)
-- VALUES (1, 2, 101, 1);

-- 7. 此时 V1 的审批实例和意见完全保留，V2 有独立的审批流
-- 两者完全隔离，互不干扰
