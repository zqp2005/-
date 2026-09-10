-- =====================================================================
-- business-flow.sql 业务流程改造数据库变更
-- 目标库：hehjiayun_community
-- 内容：
--   1) 投诉建议表 hjy_suggest（在用表）新增状态与处理字段
--   2) 新增字典类型 hjy_repair_state（6 项）、hjy_complaint_state（4 项）
--   3) 新增动作按钮权限（sys_menu F 型按钮，为报修状态机/投诉处理流/绑定审核流准备）
--
-- 执行前核查记录（2026-09-10）：
--   * 父菜单 ID 由下面查询确定（F 型按钮必须挂在 C 型菜单下）：
--       SELECT menu_id, menu_name, perms FROM sys_menu
--       WHERE menu_name LIKE '%报修%' OR menu_name LIKE '%投诉%'
--          OR menu_name LIKE '%绑定%' OR perms LIKE '%ownerRoom%';
--     结果：2061=报修信息(system:repair:list, C)
--           2054=投诉建议(system:suggest:list, C)
--           2027=业主审核(system:ownerRoom:list, C)
--   * system:ownerRoom:audit 在 sys_menu 中不存在（/audit 端点的
--     @PreAuthorize 已使用该权限串），故本脚本插入该按钮权限。
--   * sys_dict_type.dict_id / sys_dict_data.dict_code 均为自增主键，INSERT 不写。
--   * hjy_repair_state / hjy_complaint_state 的字典类型与数据均不存在，无重复插入。
-- =====================================================================

-- 1) 投诉表新增状态与处理字段
ALTER TABLE hjy_suggest
  ADD COLUMN complaint_state varchar(20) NOT NULL DEFAULT 'Pending' COMMENT '投诉状态',
  ADD COLUMN handle_by       varchar(64) COMMENT '处理人',
  ADD COLUMN handle_time     datetime    COMMENT '处理时间',
  ADD COLUMN reply_content   varchar(1000) COMMENT '回复内容';

-- 2) 新增字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, remark)
VALUES ('报修状态', 'hjy_repair_state', '0', 'admin', '报修工单状态'),
       ('投诉状态', 'hjy_complaint_state', '0', 'admin', '投诉处理状态');

-- 3) 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by) VALUES
(1,'待处理','Pending','hjy_repair_state','0','admin'),
(2,'已分派','Allocated','hjy_repair_state','0','admin'),
(3,'处理中','Processing','hjy_repair_state','0','admin'),
(4,'已处理','Processed','hjy_repair_state','0','admin'),
(5,'不处理','No_Processed','hjy_repair_state','0','admin'),
(6,'已取消','Cancelled','hjy_repair_state','0','admin'),
(1,'待受理','Pending','hjy_complaint_state','0','admin'),
(2,'处理中','Processing','hjy_complaint_state','0','admin'),
(3,'已回复','Replied','hjy_complaint_state','0','admin'),
(4,'已关闭','Closed','hjy_complaint_state','0','admin');

-- 4) 动作按钮权限（F 型按钮；parent_id 为上方核查所得的实际菜单 ID）
--    2061=报修信息、2054=投诉建议、2027=业主审核
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, menu_type, visible, status, perms, create_by)
VALUES ('报修派单', 2061, 1, '', '', 1, 'F', '0', '0', 'system:repair:assign', 'admin'),
       ('报修接单', 2061, 2, '', '', 1, 'F', '0', '0', 'system:repair:receive', 'admin'),
       ('报修完成', 2061, 3, '', '', 1, 'F', '0', '0', 'system:repair:complete', 'admin'),
       ('报修取消', 2061, 4, '', '', 1, 'F', '0', '0', 'system:repair:cancel', 'admin'),
       ('报修不处理', 2061, 5, '', '', 1, 'F', '0', '0', 'system:repair:reject', 'admin'),
       ('投诉受理', 2054, 1, '', '', 1, 'F', '0', '0', 'system:suggest:accept', 'admin'),
       ('投诉回复', 2054, 2, '', '', 1, 'F', '0', '0', 'system:suggest:reply', 'admin'),
       ('投诉关闭', 2054, 3, '', '', 1, 'F', '0', '0', 'system:suggest:close', 'admin'),
       ('绑定审核', 2027, 1, '', '', 1, 'F', '0', '0', 'system:ownerRoom:audit', 'admin');

-- =====================================================================
-- 执行后验证（人工核对）：
--   SHOW COLUMNS FROM hjy_suggest LIKE 'complaint_state';           -- 1 行
--   SELECT COUNT(*) FROM sys_dict_data
--     WHERE dict_type IN ('hjy_repair_state','hjy_complaint_state'); -- 10
--   SELECT menu_id, menu_name, parent_id, perms FROM sys_menu
--     WHERE perms IN ('system:repair:assign','system:repair:receive',
--                     'system:repair:complete','system:repair:cancel',
--                     'system:repair:reject','system:suggest:accept',
--                     'system:suggest:reply','system:suggest:close',
--                     'system:ownerRoom:audit');                     -- 9 行
-- =====================================================================

-- 5) 补齐 hjy_repair 既有缺口：代码（domain/mapper 初始提交起）引用 owner_real_name/owner_phone_number，
--    但本地库缺这两列导致 selectRepairById/list 直接 SQL 错误（Task 3 冒烟时发现）
ALTER TABLE hjy_repair
  ADD COLUMN owner_real_name      varchar(50) COMMENT '业主姓名',
  ADD COLUMN owner_phone_number   varchar(20) COMMENT '业主手机号';
