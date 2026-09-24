# 合家云社区物业管理平台

基于 Spring Boot + Vue + 微信小程序的社区物业管理平台，集成 AI 智能客服助手。包含**报修工单状态机、投诉处理流、业主入住审核流、级联数据保护、操作/登录审计日志**等真实业务流程，**管理后台（Web）+ 业主端（小程序）双端架构**，以及**登录鉴权、按用户隔离记忆**的 AI 对话服务。

## 项目架构

```
hejiayun_ui (Vue 2 + Element UI, 端口 80, 独立仓库)
    │
    ├── /hejiayun/* ──→ hjy-community (Spring Boot 2.7, 端口 8080)
    │                        │
    │                        ├── MySQL + Redis + MyBatis-Plus
    │                        └── Spring Security + JWT 认证 + RBAC 权限
    │
    └── /ai/* ────────→ hjy-ai-service (Spring Boot 3.2, 端口 8090)
                             │  （JWT 透传鉴权：校验主后端登录态）
                             ├── DeepSeek (Spring AI + Function Calling)
                             ├── Redis (按用户隔离的会话记忆)
                             ├── HTTP ──→ hjy-community (读写真实业务数据)
                             │
                             └── MCP Client（SSE）
                                    └──→ hjy-mcp-server (Spring Boot 3.2, 端口 8091)
                                          ├─ query_login_location：登录地址定位（公网IP→高德归属地，
                                          │   本地内网→当前网络出口所在地，兜底社区注册地址）
                                          └─ query_weather：城市实时天气（高德）

hij_wweixin (原生微信小程序, 业主端, 独立目录)
    │
    └── /app/* ───────→ hjy-community (8080)
                          （业主 JWT 独立签发存 owner_tokens, 与管理端令牌隔离）
```

## 核心业务功能

### 报修工单状态机

六状态流转由专用动作接口驱动，非法转移自动拒绝，时间戳与操作人服务端自动记录（前端不可伪造）：

```
待处理 ──派单──> 已分派 ──接单──> 处理中 ──完成──> 已处理
   │                │
   ├──取消──> 已取消 ├──取消──> 已取消
   └──不处理(需填原因)──> 不处理
```

- 工单号自动生成（RX + 时间戳），状态由后端强制，普通编辑接口对流转字段降级（只允许改内容）
- 派单记录维修人，完成自动记录处理人姓名/ID/电话

### 投诉建议处理流

```
待受理 ──受理──> 处理中 ──回复(填处理结果)──> 已处理 ──关闭──> 已关闭
   └──────────── 不予受理(必填原因) ────────────────┘
```

### 房屋关联审核流（入住解耦待实施）

- 新增绑定（人+房+身份）→ **审核中** → 审核通过/**驳回**（意见必填，全程留痕）
- 重复绑定校验（同人同房，驳回后可重新提交）
- **当前旧实现仍联动房间状态**：审核通过 → 房间"已入住"；解绑后无人 → 自动回"未入住"。已确认的新规则是“关联不等于入住”，将另批增加入住登记并迁移，不直接推断或改写历史数据。
- 解绑自动写入审核记录（audit_type=unbind）

### 数据保护：级联删除校验

社区 → 楼栋 → 单元 → 房间 → 业主五级删除前检查下级数据，有数据整批拒绝并提示具体数量。

### 审计日志

- **登录日志**：成功/失败（含验证码错误、密码错误）记录 IP、浏览器、时间
- **操作日志**：`@Log` 注解 + AOP 切面自动记录——谁、什么时间、对什么模块、做了什么操作、参数、结果、异常栈；覆盖全部业务写操作与状态动作

### 业主端小程序（/app 接口层）

微信小程序（原生，独立目录 `hij_wweixin`）对接主后端独立的 `/app/**` 接口层，业主自助办事：

- **注册限制**：新手机号可注册；已注册手机号拒绝；存量业主（密码为空）禁止通过公开注册认领，返回联系物业核验身份的提示。可信激活凭证流程尚未上线，不能通过普通档案编辑设置密码绕过限制。
- **服务端校验**：手机号和 18 位身份证格式正则，性别/年龄范围校验（尚非身份证校验位算法）；仅 `Enable` 状态允许登录。新手机号自助注册尚无号码归属验证，不等于房屋身份认证。
- **工单入口**：提交报修、提交投诉、查询自己的工单；当前历史归属查询仍基于姓名/手机号，稳定居民 ID 隔离和历史归属迁移待实施，不能视为已经完成严格数据隔离。
- **密码保护**：业主实体的 `ownerPassword` 不参与 JSON 序列化/反序列化；登录仍由专用请求 DTO 读取密码并使用数据库哈希校验。
- **双令牌隔离**：业主 JWT 存 `owner_tokens:{uuid}`，管理端 `login_tokens:{uuid}`，两套体系互不通用（业主 token 调管理端接口一律 401）

## AI 智能助手

| 能力 | 说明 |
|------|------|
| Function Calling | 6 大工具类调用业务接口；提示词不能代替授权或数据准确性校验，已发现问题待后续批次修复 |
| MCP 工具扩展 | 经 MCP 协议（SSE）挂载独立的 hjy-mcp-server：登录地址定位 + 高德天气，AI 自动组合调用（如"我在哪→查该地天气"） |
| 通用对话 | 物业问题调工具，闲聊/知识问答正常回答 |
| 登录鉴权 | 校验主后端 JWT（共享 Redis 校验登录态），未登录无 AI 入口 |
| 会话隔离 | 记忆按 `userId:sessionId` 存 Redis，重启不丢、用户互不可见 |
| 流式输出 | SSE 打字机效果，`[DONE]` 结束标记 |
| 故障自愈 | 调用主后端遇 401 自动重登重试，不再把故障吞成"查无数据" |

工具代码包含查询、创建报修/投诉/访客、取消报修以及 MCP 登录定位/天气入口。**当前 AI 仍使用共享服务账号调用后端，调用者业务授权尚待修复；物业费工具未接入真实账单，不可作为欠费依据。** 本批不修改 AI 服务，不代表上述风险已解决；不应向低权限账号开放这些能力用于真实业务操作。

## 快速开始

### 环境要求

- JDK 8（hjy-community）/ JDK 17+（hjy-ai-service）
- MySQL 8.0、Redis 6+
- DeepSeek API Key

### 1. 本地凭据配置（真实密码不进仓库）

```bash
# 三个后端各自复制模板并填入真实值（已被 .gitignore 排除）
cp hjy-community/src/main/resources/application-local.yml.template hjy-community/src/main/resources/application-local.yml
cp hjy-ai-service/src/main/resources/application-local.yml.template hjy-ai-service/src/main/resources/application-local.yml
cp hjy-mcp-server/src/main/resources/application-local.yml.template hjy-mcp-server/src/main/resources/application-local.yml
# 填入 MySQL 账号密码、JWT 密钥（community 与 ai-service 的 JWT 密钥必须一致）、高德 Web 服务 Key（mcp-server）
```

高德 Key 免费获取：[lbs.amap.com](https://lbs.amap.com) 注册 → 控制台 → 创建应用 → 添加 Key（类型选"Web服务"）。

### 2. 数据库初始化

创建库 `hehjiayun_community` 后执行 `hjy-community/sql/` 下的初始化脚本与 `business-flow.sql`（业务流程改造的表结构与字典变更）。

### 3. 启动（注意顺序：MCP 服务需在 AI 服务之前）

```bash
# Redis 先启动，然后：

# MCP 工具服务（JDK 17+）
cd hjy-mcp-server && mvn spring-boot:run         # localhost:8091

# 主后端（JDK 8）
cd hjy-community && mvn spring-boot:run          # localhost:8080

# AI 服务（JDK 17+，需 DEEPSEEK_API_KEY 环境变量；启动时连接 8091，
# 不需要 MCP 功能时可设 MCP_CLIENT_ENABLED=false 跳过连接）
cd hjy-ai-service && mvn spring-boot:run         # localhost:8090

# 前端（独立仓库）
cd hejiayun_ui && npm run dev                    # localhost:80
```

登录 admin/admin123。管理员可登录后台处理工单；AI 助手悬浮窗登录后可见。

## 安全机制

- **JWT 无状态认证** + `@PreAuthorize` 方法级权限 + 前端 `v-hasPermi` 按钮控制（RBAC 三级角色：超管/社区服务/只读）
- **编辑降级**：状态流转字段只能走动作接口，普通修改一律忽略，杜绝越权改状态
- **AI 服务鉴权**：JWT 透传验签 + Redis 登录态校验，会话记忆按用户物理隔离
- **双端令牌隔离**：管理端（Web）与业主端（小程序）JWT 各自独立签发/存储，跨端访问一律 401
- **审计留痕**：登录/操作日志 + 绑定审核记录全链路可追溯
- 本地凭据（MySQL 密码、JWT 密钥、DeepSeek Key）全部走 `application-local.yml` / 环境变量，不进仓库

## 业务优化进度与每批交付记录

业务基线见 `doc/业务规则与分批实施基线.md`。已确认“房屋关联不等于入住”，按安全基础、人房关系、居民认证、工单闭环、消息统计分批实施。每批同步更新对应仓库 README，区分已实现、已验证、待实施；后端/前端验证后分别提交推送，小程序不提交，不擅自修改 dev。

### 2026-09-24：第 1A 批——管理端权限与居民账号安全

已实现：

- 为用户、角色、菜单、部门、岗位、字典等原先缺失校验的方法，以及社区写操作和相关导出入口增加方法级权限（沿用现有权限字符串）。字典值查询仍供已登录业务页面使用。
- 账号新增/修改/删除/重置密码/停用，以及角色、菜单写操作，除对应动作权限外，暂时要求当前平台管理员身份（用户 ID 1）。这是授权委派规则完成前的安全限制，不能凭角色名为 admin 或自行获得通配权限绕过。
- 保留派单页对 `/system/user/list` 的兼容：仅有 `system:repair:assign` 时只查询启用人员，并仅返回 `userId/userName/nickName`；完整列表需要 `system:user:list`。这仍非最终的小区维修人员范围控制，后续单独完善。
- 阻止手机号认领存量居民档案、禁止停用/未知状态账号重新登录；屏蔽业主密码 JSON 输出和普通 JSON 编辑入口。

验证：JDK 8 下运行以下定向测试，21 项通过（新增安全测试 13 项、既有状态流转测试 8 项）。安全测试使用 Mock 服务和真实 Spring 方法安全代理，不连接数据库/Redis，不执行真实改密、删除或账号激活。测试日志仅输出控制台。

```powershell
cd hjy-community
mvn "-Dtest=OwnerAccountSecurityTest,ManagementAuthorizationTest,RepairStateFlowTest,SuggestStateFlowTest" test
```

部署注意：本批无数据库迁移，不需要改表，不修改历史数据；重新构建并重启主后端后生效。已有前端派单调用兼容，未改前端或小程序。非平台管理员不能再管理账号与授权；未激活的存量居民暂不能自助激活。尚未执行云端部署或完整浏览器端到端验收。

剩余事项：旧登录态撤销、AI 调用者授权/虚假欠费结果、Druid 匿名暴露、社区数据范围、真实身份核验、绑定与入住解耦、数据库约束/历史数据核验、房屋字典契约、状态并发与工单验收等仍待后续批次。**第 1A 批完成不代表全部审查问题已经修复。**

### 2026-09-24：补交房间导出状态修复

- 收录此前未提交的“未出售/已交房”导出需求，并修正 EasyPOI `replace` 对含下划线状态编码的拆分问题；在导出 DTO 显式转换四种中文状态，不改变数据库状态值。
- 验证：JDK 8 执行 `mvn "-Dtest=RoomExcelExportTest" test`，2 项通过。测试直接检查内存中生成的 Excel 单元格，并验证未知/空状态保留，不访问数据库。
- 原有 `hjy-community/sql/backup/` 备份及 `hjy-community/sql/data-repair-20260924.sql` 保留本地、不执行、不提交：含居民资料，且修复脚本混有推断性数据填充，未经脱敏和业务核验不能作为正式迁移发布。
- 本次仅后端代码与文档补交，无数据库变更、无云端部署，dev 分支不变。

## 提交风格

中文提交信息，以功能描述为主。
