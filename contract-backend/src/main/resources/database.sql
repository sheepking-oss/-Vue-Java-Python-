-- =============================================
-- 企业合同审批与归档协同平台数据库脚本
-- 数据库：MySQL 8.0+
-- 创建日期：2026-04-27
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS contract_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE contract_platform;

-- =============================================
-- 1. 系统管理模块表
-- =============================================

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(50) COMMENT '部门编码',
    sort INT DEFAULT 0 COMMENT '显示排序',
    leader VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态（0停用 1正常）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_dept_code (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dept_id BIGINT COMMENT '部门ID',
    user_name VARCHAR(30) NOT NULL COMMENT '用户账号',
    nick_name VARCHAR(30) NOT NULL COMMENT '用户昵称',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    sex TINYINT DEFAULT 0 COMMENT '性别（0男 1女 2未知）',
    avatar VARCHAR(255) DEFAULT '' COMMENT '头像地址',
    status TINYINT DEFAULT 1 COMMENT '状态（0停用 1正常）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dept_id (dept_id),
    INDEX idx_user_name (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_name VARCHAR(30) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(100) NOT NULL COMMENT '角色权限字符串',
    sort INT DEFAULT 0 COMMENT '显示顺序',
    data_scope TINYINT DEFAULT 1 COMMENT '数据范围（1：全部数据权限 2：自定义数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限）',
    status TINYINT DEFAULT 1 COMMENT '状态（0停用 1正常）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_type TINYINT DEFAULT 1 COMMENT '菜单类型（M目录 C菜单 F按钮）',
    path VARCHAR(200) DEFAULT '' COMMENT '路由地址',
    component VARCHAR(255) DEFAULT '' COMMENT '组件路径',
    perms VARCHAR(100) DEFAULT '' COMMENT '权限标识',
    visible TINYINT DEFAULT 1 COMMENT '菜单状态（0隐藏 1显示）',
    sort INT DEFAULT 0 COMMENT '显示顺序',
    icon VARCHAR(100) DEFAULT '' COMMENT '菜单图标',
    status TINYINT DEFAULT 1 COMMENT '状态（0停用 1正常）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限菜单表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, permission_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- =============================================
-- 2. 合同管理模块表
-- =============================================

-- 合同类型表
CREATE TABLE IF NOT EXISTS contract_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    type_name VARCHAR(100) NOT NULL COMMENT '合同类型名称',
    type_code VARCHAR(50) NOT NULL COMMENT '合同类型编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父类型ID',
    description VARCHAR(500) COMMENT '描述',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态（0停用 1正常）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_type_code (type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同类型表';

-- 合同主表
CREATE TABLE IF NOT EXISTS contract (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_no VARCHAR(50) NOT NULL COMMENT '合同编号',
    contract_name VARCHAR(200) NOT NULL COMMENT '合同名称',
    type_id BIGINT COMMENT '合同类型ID',
    party_a VARCHAR(200) NOT NULL COMMENT '甲方',
    party_b VARCHAR(200) NOT NULL COMMENT '乙方',
    contract_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '合同金额',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
    start_date DATE COMMENT '合同开始日期',
    end_date DATE COMMENT '合同结束日期',
    sign_date DATE COMMENT '签订日期',
    contract_content TEXT COMMENT '合同内容摘要',
    current_version INT DEFAULT 1 COMMENT '当前版本号',
    status TINYINT DEFAULT 0 COMMENT '合同状态（0草稿 1审批中 2已通过 3已驳回 4已退回 5已归档 6已到期）',
    dept_id BIGINT COMMENT '所属部门ID',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
    INDEX idx_contract_no (contract_no),
    INDEX idx_status (status),
    INDEX idx_create_by (create_by),
    INDEX idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同主表';

-- 合同版本表
CREATE TABLE IF NOT EXISTS contract_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_id BIGINT NOT NULL COMMENT '合同ID',
    version_no INT NOT NULL COMMENT '版本号',
    contract_no VARCHAR(50) NOT NULL COMMENT '合同编号',
    contract_name VARCHAR(200) NOT NULL COMMENT '合同名称',
    type_id BIGINT COMMENT '合同类型ID',
    party_a VARCHAR(200) NOT NULL COMMENT '甲方',
    party_b VARCHAR(200) NOT NULL COMMENT '乙方',
    contract_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '合同金额',
    start_date DATE COMMENT '合同开始日期',
    end_date DATE COMMENT '合同结束日期',
    sign_date DATE COMMENT '签订日期',
    contract_content TEXT COMMENT '合同内容摘要',
    change_reason VARCHAR(500) COMMENT '变更原因',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_contract_id (contract_id),
    INDEX idx_version_no (contract_id, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同版本表';

-- 合同附件表
CREATE TABLE IF NOT EXISTS contract_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_id BIGINT NOT NULL COMMENT '合同ID',
    version_id BIGINT COMMENT '版本ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名称',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_type VARCHAR(50) COMMENT '文件类型',
    description VARCHAR(500) COMMENT '文件描述',
    create_by BIGINT COMMENT '上传人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
    INDEX idx_contract_id (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同附件表';

-- =============================================
-- 3. 审批流程模块表
-- =============================================

-- 审批流程定义表
CREATE TABLE IF NOT EXISTS approval_flow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    flow_name VARCHAR(100) NOT NULL COMMENT '流程名称',
    flow_code VARCHAR(50) NOT NULL COMMENT '流程编码',
    flow_type VARCHAR(50) NOT NULL COMMENT '流程类型（合同审批、采购审批等）',
    description VARCHAR(500) COMMENT '流程描述',
    status TINYINT DEFAULT 1 COMMENT '状态（0停用 1启用）',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
    INDEX idx_flow_code (flow_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程定义表';

-- 审批节点定义表
CREATE TABLE IF NOT EXISTS approval_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    flow_id BIGINT NOT NULL COMMENT '流程ID',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    node_code VARCHAR(50) NOT NULL COMMENT '节点编码',
    node_type TINYINT NOT NULL COMMENT '节点类型（1审批节点 2抄送节点 3条件节点 4结束节点）',
    sort INT NOT NULL COMMENT '节点顺序',
    approve_type TINYINT COMMENT '审批方式（1或签 2会签 3依次审批）',
    assignee_type TINYINT COMMENT '审批人类型（1指定用户 2指定角色 3部门负责人 4发起人自选）',
    assignee_value VARCHAR(500) COMMENT '审批人值（用户ID/角色ID/部门ID等，多个用逗号分隔）',
    conditions VARCHAR(1000) COMMENT '条件表达式（JSON格式，用于条件节点）',
    description VARCHAR(500) COMMENT '节点描述',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_flow_id (flow_id),
    INDEX idx_sort (flow_id, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批节点定义表';

-- 审批实例表
CREATE TABLE IF NOT EXISTS approval_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    business_id BIGINT NOT NULL COMMENT '业务ID（合同ID等）',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    flow_id BIGINT NOT NULL COMMENT '流程定义ID',
    instance_name VARCHAR(200) NOT NULL COMMENT '实例名称',
    current_node_id BIGINT COMMENT '当前节点ID',
    current_node_name VARCHAR(100) COMMENT '当前节点名称',
    status TINYINT DEFAULT 1 COMMENT '状态（0已撤销 1审批中 2已通过 3已驳回 4已退回）',
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_by BIGINT NOT NULL COMMENT '发起人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_business_id (business_id, business_type),
    INDEX idx_create_by (create_by),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批实例表';

-- 审批实例节点记录表
CREATE TABLE IF NOT EXISTS approval_instance_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    instance_id BIGINT NOT NULL COMMENT '审批实例ID',
    node_id BIGINT NOT NULL COMMENT '节点定义ID',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    node_type TINYINT NOT NULL COMMENT '节点类型',
    sort INT NOT NULL COMMENT '节点顺序',
    assignee_type TINYINT COMMENT '审批人类型',
    assignee_value VARCHAR(500) COMMENT '审批人值',
    actual_assignee BIGINT COMMENT '实际审批人ID',
    status TINYINT DEFAULT 0 COMMENT '状态（0待处理 1处理中 2已通过 3已驳回 4已退回 5已跳过）',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    comment TEXT COMMENT '审批意见',
    INDEX idx_instance_id (instance_id),
    INDEX idx_actual_assignee (actual_assignee)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批实例节点记录表';

-- 审批意见表
CREATE TABLE IF NOT EXISTS approval_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    instance_id BIGINT NOT NULL COMMENT '审批实例ID',
    node_id BIGINT COMMENT '节点ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人名称',
    action_type TINYINT NOT NULL COMMENT '操作类型（1通过 2驳回 3退回 4撤回 5抄送）',
    comment TEXT COMMENT '审批意见',
    attachments VARCHAR(1000) COMMENT '附件（JSON格式）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_instance_id (instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批意见表';

-- =============================================
-- 4. 归档管理模块表
-- =============================================

-- 归档记录表
CREATE TABLE IF NOT EXISTS archive_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_id BIGINT NOT NULL COMMENT '合同ID',
    contract_no VARCHAR(50) NOT NULL COMMENT '合同编号',
    contract_name VARCHAR(200) NOT NULL COMMENT '合同名称',
    archive_no VARCHAR(50) NOT NULL COMMENT '归档编号',
    archive_type TINYINT DEFAULT 1 COMMENT '归档类型（1电子归档 2纸质归档）',
    storage_location VARCHAR(500) COMMENT '存放位置',
    archive_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    archive_by BIGINT COMMENT '归档人ID',
    archive_by_name VARCHAR(50) COMMENT '归档人名称',
    remark VARCHAR(500) COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态（1已归档 2已借出 3已归还 4已销毁）',
    borrow_by BIGINT COMMENT '借阅人ID',
    borrow_time DATETIME COMMENT '借阅时间',
    expected_return_time DATETIME COMMENT '预计归还时间',
    actual_return_time DATETIME COMMENT '实际归还时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_contract_id (contract_id),
    INDEX idx_archive_no (archive_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归档记录表';

-- =============================================
-- 5. 到期提醒模块表
-- =============================================

-- 合同提醒表
CREATE TABLE IF NOT EXISTS contract_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_id BIGINT NOT NULL COMMENT '合同ID',
    alert_type TINYINT NOT NULL COMMENT '提醒类型（1即将到期 2已到期 3付款提醒 4其他）',
    alert_date DATE NOT NULL COMMENT '提醒日期',
    alert_days INT COMMENT '提前提醒天数',
    alert_content VARCHAR(500) COMMENT '提醒内容',
    receiver_id BIGINT COMMENT '接收人ID',
    receiver_name VARCHAR(50) COMMENT '接收人名称',
    status TINYINT DEFAULT 0 COMMENT '状态（0未提醒 1已提醒 2已处理 3已忽略）',
    alert_time DATETIME COMMENT '提醒时间',
    handle_time DATETIME COMMENT '处理时间',
    handle_remark VARCHAR(500) COMMENT '处理备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_contract_id (contract_id),
    INDEX idx_alert_date (alert_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同提醒表';

-- =============================================
-- 6. 初始化数据
-- =============================================

-- 插入部门数据
INSERT INTO sys_dept (id, parent_id, dept_name, dept_code, sort, status) VALUES
(1, 0, '总公司', 'HQ', 0, 1),
(2, 1, '行政部', 'ADMIN', 1, 1),
(3, 1, '财务部', 'FINANCE', 2, 1),
(4, 1, '法务部', 'LEGAL', 3, 1),
(5, 1, '业务部', 'BUSINESS', 4, 1),
(6, 5, '销售一部', 'SALES1', 1, 1),
(7, 5, '销售二部', 'SALES2', 2, 1);

-- 插入角色数据
INSERT INTO sys_role (id, role_name, role_key, sort, data_scope, status) VALUES
(1, '超级管理员', 'admin', 1, 1, 1),
(2, '普通用户', 'user', 2, 5, 1),
(3, '部门经理', 'manager', 3, 4, 1),
(4, '法务专员', 'legal', 4, 3, 1),
(5, '财务专员', 'finance', 5, 3, 1),
(6, '档案管理员', 'archiver', 6, 1, 1);

-- 插入用户数据（密码：123456，使用 BCrypt 加密）
INSERT INTO sys_user (id, dept_id, user_name, nick_name, password, email, phone, sex, status) VALUES
(1, 1, 'admin', '系统管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'admin@company.com', '13800138000', 0, 1),
(2, 4, 'legal01', '张法务', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'legal01@company.com', '13800138001', 1, 1),
(3, 3, 'finance01', '李财务', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'finance01@company.com', '13800138002', 0, 1),
(4, 6, 'sales01', '王销售', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'sales01@company.com', '13800138003', 0, 1);

-- 插入用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 4),
(3, 5),
(4, 2);

-- 插入合同类型
INSERT INTO contract_type (id, type_name, type_code, parent_id, sort, status) VALUES
(1, '采购合同', 'PURCHASE', 0, 1, 1),
(2, '销售合同', 'SALES', 0, 2, 1),
(3, '服务合同', 'SERVICE', 0, 3, 1),
(4, '租赁合同', 'LEASE', 0, 4, 1),
(5, '合作协议', 'COOPERATION', 0, 5, 1);

-- 插入审批流程定义（合同审批流程）
INSERT INTO approval_flow (id, flow_name, flow_code, flow_type, description, status) VALUES
(1, '合同审批流程', 'CONTRACT_APPROVAL', 'contract', '标准合同审批流程：提交申请 -> 部门经理审批 -> 法务审核 -> 财务审核 -> 总经理审批 -> 归档', 1);

-- 插入审批节点定义
INSERT INTO approval_node (id, flow_id, node_name, node_code, node_type, sort, approve_type, assignee_type, assignee_value, description) VALUES
(1, 1, '发起人提交', 'START', 4, 1, NULL, NULL, NULL, '开始节点'),
(2, 1, '部门经理审批', 'DEPT_MANAGER', 1, 2, 1, 3, NULL, '部门负责人审批'),
(3, 1, '法务审核', 'LEGAL_AUDIT', 1, 3, 1, 2, '4', '法务专员审核，角色ID为4'),
(4, 1, '财务审核', 'FINANCE_AUDIT', 1, 4, 1, 2, '5', '财务专员审核，角色ID为5'),
(5, 1, '总经理审批', 'GM_APPROVAL', 1, 5, 1, 2, '1', '总经理审批，角色ID为1'),
(6, 1, '流程结束', 'END', 4, 6, NULL, NULL, NULL, '结束节点');
