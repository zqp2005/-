# 合家云社区物业管理平台

基于 Spring Boot + Vue 的社区物业管理平台，集成 AI 智能客服助手。包含**报修工单状态机、投诉处理流、业主入住审核流、级联数据保护、操作/登录审计日志**等真实业务流程，以及**登录鉴权、按用户隔离记忆**的 AI 对话服务。

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
                             └── HTTP ──→ hjy-community (读写真实业务数据)
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

### 业主入住绑定审核流

- 新增绑定（人+房+身份）→ **审核中** → 审核通过/**驳回**（意见必填，全程留痕）
- 重复绑定校验（同人同房，驳回后可重新提交）
- **房间状态自动联动**：审核通过 → 房间"已入住"；解绑后无人 → 自动回"未入住"
- 解绑自动写入审核记录（audit_type=unbind）

### 数据保护：级联删除校验

社区 → 楼栋 → 单元 → 房间 → 业主五级删除前检查下级数据，有数据整批拒绝并提示具体数量。

### 审计日志

- **登录日志**：成功/失败（含验证码错误、密码错误）记录 IP、浏览器、时间
- **操作日志**：`@Log` 注解 + AOP 切面自动记录——谁、什么时间、对什么模块、做了什么操作、参数、结果、异常栈；覆盖全部业务写操作与状态动作

## AI 智能助手

| 能力 | 说明 |
|------|------|
| Function Calling | 6 大工具类调真实接口取数，杜绝幻觉（系统提示词外置 `system-prompt.txt`） |
| 登录鉴权 | 校验主后端 JWT（共享 Redis 校验登录态），未登录无 AI 入口 |
| 会话隔离 | 记忆按 `userId:sessionId` 存 Redis，重启不丢、用户互不可见 |
| 流式输出 | SSE 打字机效果，`[DONE]` 结束标记 |
| 故障自愈 | 调用主后端遇 401 自动重登重试，不再把故障吞成"查无数据" |

工具能力（诚实版）：查询（报修/投诉/物业费/业主/车辆/访客/公告/社区设施等 17 项）+ 创建（报修/投诉/访客登记）+ 取消报修（走状态机合法流转）。

## 快速开始

### 环境要求

- JDK 8（hjy-community）/ JDK 17+（hjy-ai-service）
- MySQL 8.0、Redis 6+
- DeepSeek API Key

### 1. 本地凭据配置（真实密码不进仓库）

```bash
# 两个后端各自复制模板并填入真实值（已被 .gitignore 排除）
cp hjy-community/src/main/resources/application-local.yml.template hjy-community/src/main/resources/application-local.yml
cp hjy-ai-service/src/main/resources/application-local.yml.template hjy-ai-service/src/main/resources/application-local.yml
# 填入 MySQL 账号密码、JWT 密钥（两个服务的 JWT 密钥必须一致，AI 服务靠它验签）
```

### 2. 数据库初始化

创建库 `hehjiayun_community` 后执行 `hjy-community/sql/` 下的初始化脚本与 `business-flow.sql`（业务流程改造的表结构与字典变更）。

### 3. 启动

```bash
# 主后端（JDK 8）
cd hjy-community && mvn spring-boot:run          # localhost:8080

# AI 服务（JDK 17+，需 DEEPSEEK_API_KEY 环境变量）
cd hjy-ai-service && mvn spring-boot:run         # localhost:8090

# 前端（独立仓库）
cd hejiayun_ui && npm run dev                    # localhost:80
```

登录 admin/admin123。管理员可登录后台处理工单；AI 助手悬浮窗登录后可见。

## 安全机制

- **JWT 无状态认证** + `@PreAuthorize` 方法级权限 + 前端 `v-hasPermi` 按钮控制（RBAC 三级角色：超管/社区服务/只读）
- **编辑降级**：状态流转字段只能走动作接口，普通修改一律忽略，杜绝越权改状态
- **AI 服务鉴权**：JWT 透传验签 + Redis 登录态校验，会话记忆按用户物理隔离
- **审计留痕**：登录/操作日志 + 绑定审核记录全链路可追溯
- 本地凭据（MySQL 密码、JWT 密钥、DeepSeek Key）全部走 `application-local.yml` / 环境变量，不进仓库

## 提交风格

中文提交信息，以功能描述为主。
