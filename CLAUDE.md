# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

**合家云社区 (HeJiaYun)** —— 社区物业管理平台，包含两个独立的 Spring Boot 服务、一个 MCP 工具服务、一个 Vue 2 前端和一个微信小程序。**前端与小程序均不在本仓库内**：前端位于 `D:\Uilted\hjy_ui\hejiayun_ui`，小程序位于 `D:\Uilted\hij_wweixin`。

## 仓库结构

```
hjy/
├── hjy-community/       # 主物业管理后端 (Spring Boot 2.7.8, Java 8)
├── hjy-ai-service/       # AI 智能助手服务 (Spring Boot 3.2.5, Java 17)
├── hjy-mcp-server/       # MCP 工具服务 (Spring Boot 3.2.5, Java 17, 端口 8091：登录地址定位 + 高德天气)
├── generate-pptx.js      # 辅助脚本：生成项目介绍 PPT（根目录 package.json 是它的依赖，与主项目无关）
└── AGENTS.md             # 内容与本文件基本同步（供 Codex 使用）

前端：D:\Uilted\hjy_ui\hejiayun_ui（Vue 2 + Element UI，独立目录，不在本仓库）
小程序：D:\Uilted\hij_wweixin（原生微信小程序，业主端，独立目录，不在本仓库）
```

## 构建和运行命令

### hjy-community（主后端）
```bash
cd hjy-community
mvn clean package -DskipTests          # 构建
mvn spring-boot:run                    # 运行（端口 8080）
mvn test                               # 运行测试（需本地 MySQL/Redis 在线，否则上下文加载失败）
mvn test -Dtest=类名                    # 运行单个测试类
```

### hjy-ai-service（AI 后端）
```bash
cd hjy-ai-service
mvn clean package -DskipTests          # 构建
mvn spring-boot:run                    # 运行（端口 8090）
```
需要环境变量 `DEEPSEEK_API_KEY`。连接主后端时仅透传当前调用者令牌，不再配置共享管理员凭据；AI 写操作在确认流程上线前拒绝。Docker 部署：`docker build -t hjy-ai-service .`。

### hjy-mcp-server（MCP 工具服务）
```bash
cd hjy-mcp-server
mvn spring-boot:run                    # 运行（端口 8091，需在 hjy-ai-service 之前启动）
```
MCP Server，经 SSE 向 AI 服务提供两个工具（登录地址定位、高德天气）。高德 Key 配在 `src/main/resources/application-local.yml`（`AMAP_KEY`，模板见同目录 .template）。AI 服务启动时会连接本服务，连不上则启动失败；不需要 MCP 工具时可设环境变量 `MCP_CLIENT_ENABLED=false` 让 AI 服务跳过连接。

### hejiayun_ui（前端，位于仓库外）
```bash
cd /d/Uilted/hjy_ui/hejiayun_ui
npm run dev                            # 开发服务器（端口 80）
npm run build:prod                     # 生产构建
npm run lint                           # 代码检查
npm run test:unit                      # 单元测试（Jest）
```
⚠️ npm scripts 内嵌的是 **Windows cmd 的 `set` 语法**（`set NODE_OPTIONS=--openssl-legacy-provider && ...`）。在 bash（Git Bash）下 `set` 是 shell 内置命令，**不会设置环境变量**，导致老版 webpack 的 OpenSSL 报错。bash 下请手动执行：
```bash
export NODE_OPTIONS=--openssl-legacy-provider
npx vue-cli-service serve
```

## 数据库

MySQL 8，本地 `127.0.0.1:3306`，账号 root/123456（真实值在 `hjy-community/src/main/resources/application-local.yml`，该文件被 gitignore 不提交）。库名为 **`hehjiayun_community`**（注意拼写是 hehjiayun）。两个后端共享此库。主要表：
- `sys_*` —— 系统管理（用户、角色、菜单、部门、字典、配置、通知、定时任务、日志）
- `hjy_*` —— 物业业务（社区、楼栋、单元、房间、业主、业主-房间关联、报修、投诉建议、访客）
- 另有独立的 `chat_memory` 库（含 `spring_ai_chat_memory` 表），但当前代码未使用 JDBC 会话存储，仅为遗留

## 架构：hjy-community

单模块 Maven 项目，按功能分包（非 Maven 子模块）。基础包路径：`com.msb.hjycommunity`。

### 分层与包结构

| 层 | 包路径 | 职责 |
|-------|---------|---------|
| **common** | `common.core.domain` | `BaseEntity`、`BaseResponse`、`R`（统一响应）、`TreeSelect` |
| | `common.core.controller` | `BaseController` —— 共享的 CRUD/分页方法 |
| | `common.core.page` | `PageDomain`、`TableDataInfo`、`TableSupport` —— 分页辅助类 |
| | `common.core.exception` | `GlobalExceptionHandler`、自定义异常 |
| | `common.utils` | `ExcelUtils`、`RedisCache` 等工具类 |
| **framework** | `framework.config` | `SecurityConfig`、Druid 配置、Swagger 配置 |
| | `framework.security` | JWT 过滤器、认证服务、退出登录/403 处理器 |
| **system** | `system` | 用户、角色、菜单、部门、字典、通知、配置、区域 |
| **community** | `community` | 社区基本信息管理 |
| **property** | `property` | 业主、楼栋、单元、房间、报修、投诉、访客 |
| **monitor** | `monitor` | 定时任务、登录日志、操作日志 |
| **web** | `web.controller.{模块}` | 各模块的 REST 控制器（common/community/monitor/property/system/app） |

### 核心模式

- **BaseController**（`common.core.controller.BaseController`）：提供 `getDataTable()` 用于分页响应，`startPage()` 用于启动 PageHelper 分页。所有控制器继承此类。
- **统一响应**：`R<T>` 封装所有 API 返回，包含 code/message/data。`BaseResponse` 是其旧版替代方案。
- **BaseEntity**：领域实体基类，通过 MyBatis-Plus 元对象处理器自动填充 `createTime`、`updateTime`、`createBy`、`updateBy`。
- **安全认证（管理端）**：`SecurityConfig` 继承 `WebSecurityConfigurerAdapter`。`JwtAuthenticationTokenFilter` 对每个请求校验 JWT。`@PreAuthorize` 注解用于方法级权限控制。`/captcha`、`/login` 无需认证即可访问；`/aiLogin` 与 `/druid/**` 已禁用。
- **业主端接口（小程序）**：`web.controller.app` 包提供 `/app/**` 独立接口层（注册/登录、提交报修投诉、查自己的单）。Spring Security 对 `/app/**` `permitAll`，认证由 `AppAuthFilter` 自管：签发/校验**独立于管理端的业主 JWT**（Redis key `owner_tokens:{uuid}`，与管理端 `login_tokens` 完全隔离），注册白名单（register/login）、手机号/身份证正则校验，禁止凭自填资料激活存量空密码业主，暂需联系物业核验；每次请求复核账号状态、凭据指纹和有效期。
- **MyBatis-Plus + PageHelper**：增删改查通过 MyBatis-Plus（`BaseMapper`），分页通过 PageHelper（`PageHelper.startPage()`），复杂查询在 `resources/mapper/**/*Mapper.xml` 中（按 monitor/property/system 模块组织）。
- **DTO/VO 模式**：`domain/dto` 存放请求体，`domain/vo` 存放响应体。使用 Orika（`MapperFacade`）进行对象属性拷贝。
- **业务状态机与动作接口**：报修（`RepairState`）与投诉（`SuggestState`）的状态流转只能通过动作端点驱动（报修 `assign/receive/complete/cancel/reject`，投诉 `accept/reply/close`，均为 `PUT .../动作/{id}`），非法转移抛 `CustomException` 并自动记录时间戳/操作人；普通 update 在 Service 层置空流转字段实现编辑降级。房屋绑定走审核流（Auditing→Binding/Rejected，审核/解绑在 `hjy_owner_room_record` 留痕并联动房间入住状态）。社区/楼栋/单元/房间/业主五级删除有级联校验，有下级数据整批拒绝。

### 配置文件
- `application.yml` —— 主配置（服务器、Redis、MyBatis-Plus、JWT token）
- `application-druid.yml` —— Druid 连接池 + MySQL 数据源（账号密码为 `${MYSQL_USERNAME}`/`${MYSQL_PASSWORD}` 占位符）
- `application-local.yml` —— **本地真实凭据**（MySQL 账号密码、JWT 密钥），被 `.gitignore` 排除不提交，由 `spring.config.import` 启动时自动加载；模板为同目录 `application-local.yml.template`
- `logback.xml` —— 日志配置

本地运行还需 JDK 8（本机在 `C:\Program Files\Java\jdk1.8.0_151`，默认 shell JDK 是 21 会导致 Lombok 编译失败）和 Redis 在线（本机 Redis 在 `D:\代码及工具\Redis-x64-5.0.14.1`）。

## 架构：hjy-ai-service

Spring Boot 3.2.5 + Spring AI 1.0.0，使用 DeepSeek 模型（deepseek-chat）。基础包路径：`com.msb.hjy.ai`。

### 分层结构

| 包 | 职责 |
|---------|---------|
| `controller/` | `ChatController` —— 对话 API（同步、流式 SSE、清除会话、健康检查），全部委托 Service |
| `service/` | `ChatService` 接口 + `impl/ChatServiceImpl` —— 对话逻辑（同步与流式，含问候/帮助拦截与用户信息注入） |
| `tools/` | 6 个工具类：`RepairTool`、`ComplaintTool`、`PropertyFeeTool`、`OwnerInfoTool`、`AnnouncementTool`、`CommunityTool` |
| `client/` | `HjyCommunityClient` —— 调用 hjy-community API 的 REST 客户端（RestTemplate，仅透传当前调用者 JWT，拒绝共享管理员身份与自动重登） |
| `config/` | `ChatClientConfig`（ChatClient/ChatMemory Bean）、`RedisChatMemoryRepository`（Redis 会话存储）、`AiConfig`、`AiProperties`、`RedisConfig`、`WebConfig` |
| `prompt/` | `PromptTemplate` —— 问候/帮助常量 |
| `dto/` | `ChatRequest`、`ChatResponse` |
| `common/` | 常量、异常、统一结果封装（`constant/`、`exception/`、`result/`） |

### 核心模式

- `ChatClientConfig` 中构建 `ChatClient` Bean：注入六大工具 + `defaultSystem()` 加载 `resources/prompt/system-prompt.txt`（系统提示词外置，强制工具调用规则，避免幻觉）。
- `tools/` 包中的 `@Tool` 注解方法由 Spring AI 自动发现。AI 根据用户意图决定调用哪个工具。
- **会话记忆持久化在 Redis**：`RedisChatMemoryRepository`（key `ai:chat:memory:{sessionId}`）+ `MessageWindowChatMemory`（20 条窗口），重启不丢会话。
- Controller 统一走 Service 调用链（同步与流式的拦截逻辑、用户信息注入都在 `ChatServiceImpl`）；SSE 每元素 `data: xxx\n\n`，正常结束以 `data: [DONE]` 收尾。
- `HjyCommunityClient` 只以当前调用者令牌查询主后端；工具边界恢复/清理线程身份，查询失败不得当作空列表。真实账单未上线，不生成欠费金额；AI 写操作暂拒绝。
- WebFlux 实现流式对话（SSE），端点：`POST /ai/chat`、`POST /ai/chat/stream`、`DELETE /ai/session/{sessionId}`、`GET /ai/health`。
- 单元测试覆盖 JWT 解析、工具身份隔离、调用者令牌透传与拒绝写操作。

## 架构：hejiayun_ui（前端，实际位置 D:\Uilted\hjy_ui\hejiayun_ui）

Vue 2.6 + Element UI 后台管理模板，基于 vue-admin-template。

### 目录结构

| 目录 | 职责 |
|-----------|---------|
| `src/api/` | Axios API 调用，按后端模块组织（`system/`、`property/`、`monitor/`、`tool/`、`ai/`） |
| `src/views/` | 页面组件，与模块结构一一对应 |
| `src/router/` | Vue Router 配置，根据用户权限动态加载路由 |
| `src/store/` | Vuex 状态管理模块（`user`、`app`、`permission`、`settings`、`tagsView`） |
| `src/layout/` | 主布局，包含侧边栏、导航栏、标签页 |
| `src/components/` | 共享 UI 组件（`Pagination`、`RightToolbar`、`IconSelect`、`Editor` 等） |
| `src/directive/permission/` | `v-permission` 指令，用于按钮级权限控制 |
| `src/utils/` | `request.js`（axios 拦截器）、`auth.js`（token 存储） |

### 核心模式

- **代理**：开发服务器将 `VUE_APP_BASE_API`（开发环境为 `/hejiayun`，见 `.env.development`）代理到 `localhost:8080` 并重写掉前缀；`/ai` 代理到 `localhost:8090`。
- **认证流程**：JWT token 存储在 cookie 中（`Admin-Token`）。Axios 拦截器附加 token 并处理 401 重定向。
- **权限模型**：根据用户角色动态加载路由。`v-permission` 指令用于元素级别的权限控制。
- **增删改查页面**遵循统一模式：列表/搜索表单 → 分页表格 → 新增/编辑弹窗，通过 `Pagination` 和 `RightToolbar` 组件复用。

## 服务间通信

前端 → `/hejiayun/*` → hjy-community（8080）
前端 → `/ai/*` → hjy-ai-service（8090）
小程序 → `/app/*` → hjy-community（8080，真机调试走电脑局域网 IP，config.js 按平台自动切换 localhost/局域网 IP)
hjy-ai-service → `/system/*`、`/community/*` 等 → hjy-community（8080），通过 `HjyCommunityClient` 调用
hjy-ai-service ← SSE ← hjy-mcp-server（8091，提供登录地址定位/天气工具）

## 提交风格

中文提交信息，以功能描述为主。

## 分批交付约定

- 每批完成后同步更新对应仓库 README.md，说明已实现行为、验证结果、部署变化和仍未完成事项。
- 业务目标与分批状态见 `doc/业务规则与分批实施基线.md`；房屋关联不等于实际入住，未完成迁移前不得宣称已解耦。
- 后端、前端每批验证后仅提交本批文件并推送；小程序不提交推送；未经用户要求不动 dev 分支，保留原有未提交修改。
