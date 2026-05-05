# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目概述

**合家云社区 (HeJiaYun)** —— 社区物业管理平台，包含两个独立的 Spring Boot 服务和一个 Vue 2 前端。

## 仓库结构

```
hjy/
├── hjy-community/       # 主物业管理后端 (Spring Boot 2.7.8, Java 8)
├── hjy-ai-service/       # AI 智能助手服务 (Spring Boot 3.2.5, Java 17)
└── hejiayun_ui/          # Vue 2 + Element UI 前端
```

## 构建和运行命令

### hjy-community（后端）
```bash
cd hjy-community
mvn clean package -DskipTests          # 构建
mvn spring-boot:run                    # 运行（端口 8080）
mvn test                               # 运行测试
mvn test -Dtest=类名                    # 运行单个测试类
```

### hjy-ai-service（AI 后端）
```bash
cd hjy-ai-service
mvn clean package -DskipTests          # 构建
mvn spring-boot:run                    # 运行（端口 8090）
```
需要设置环境变量 `DEEPSEEK_API_KEY`（或在 application.yml 中配置）。
启动端口 8090，前端通过 `/ai` → `localhost:8090` 代理访问。

### hejiayun_ui（前端）
```bash
cd hejiayun_ui
npm run dev                            # 开发服务器（端口 80，需要 OpenSSL legacy provider）
npm run build:prod                     # 生产构建
npm run lint                           # 代码检查
npm run test:unit                      # 单元测试（Jest）
```

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
| **web** | `web.controller.{模块}` | 各模块的 REST 控制器 |

### 核心模式

- **BaseController**（`common.core.controller.BaseController`）：提供 `getDataTable()` 用于分页响应，`startPage()` 用于启动 PageHelper 分页。所有控制器继承此类。
- **统一响应**：`R<T>` 封装所有 API 返回，包含 code/message/data。`BaseResponse` 是其旧版替代方案。
- **BaseEntity**：领域实体基类，通过 MyBatis-Plus 元对象处理器自动填充 `createTime`、`updateTime`、`createBy`、`updateBy`。
- **安全认证**：`SecurityConfig` 继承 `WebSecurityConfigurerAdapter`。`JwtAuthenticationTokenFilter` 对每个请求校验 JWT。`@PreAuthorize` 注解用于方法级权限控制。`/captcha`、`/login`、`/aiLogin` 无需认证即可访问。
- **MyBatis-Plus + PageHelper**：增删改查通过 MyBatis-Plus（`BaseMapper`），分页通过 PageHelper（`PageHelper.startPage()`），复杂查询在 `resources/mapper/**/*Mapper.xml` 中。
- **DTO/VO 模式**：`domain/dto` 存放请求体，`domain/vo` 存放响应体。使用 Orika（`MapperFacade`）进行对象属性拷贝。

### 配置文件
- `application.yml` —— 主配置（服务器、Redis、MyBatis-Plus、JWT token）
- `application-druid.yml` —— Druid 连接池 + MySQL 数据源
- `logback.xml` —— 日志配置
- `resources/mapper/**/*Mapper.xml` —— MyBatis XML 映射文件（每实体一个，按模块组织）

## 架构：hjy-ai-service

Spring Boot 3.2.5 + Spring AI 1.0.0，使用 DeepSeek 模型。基础包路径：`com.msb.hjy.ai`。

### 分层结构

| 包 | 职责 |
|---------|---------|
| `controller/` | `ChatController`（对话 API）、`KnowledgeController`（知识库） |
| `service/` | `ChatService`（AI 对话）、`KnowledgeService`（RAG 知识检索） |
| `agent/` | `HjyAgent` —— AI 代理定义、`SystemPrompt` —— 系统提示词模板、`ToolExecutor` |
| `tools/` | 6 个工具类：`RepairTool`、`ComplaintTool`、`PropertyFeeTool`、`OwnerInfoTool`、`AnnouncementTool`、`CommunityTool` |
| `client/` | `HjyCommunityClient` —— 调用 hjy-community API 的 REST 客户端（使用 RestTemplate + JWT token） |
| `config/` | `AiConfig`、`ChatConfig`、`RedisConfig`、`WebConfig` |
| `prompt/` | 提示词模板管理 |
| `model/` | AI 对话上下文的领域模型 |
| `common/` | 常量、异常、统一结果封装 |

### 核心模式

- 使用 Spring AI 的 `ChatClient` 配合 DeepSeek 模型进行对话。
- `tools/` 包中的 `@Tool` 注解方法会被 Spring AI 自动发现用于函数调用。AI 根据用户意图决定调用哪个工具。
- `HjyCommunityClient` 以管理员身份自动认证，缓存 JWT token，并代理请求到 `hjy-community` 后端获取真实数据（报修、投诉、缴费等）。
- 使用 WebFlux 实现对话接口的流式响应（SSE）。
- Redis 用于按会话存储对话历史。
- 系统提示词在 `prompt/` 配置文件中定义，启动时加载。

## 架构：hejiayun_ui（前端）

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

- **代理**：开发服务器将 `/hejiayun` 代理到 `localhost:8080`，将 `/ai` 代理到 `localhost:8090`。
- **认证流程**：JWT token 存储在 cookie 中（`Admin-Token`）。Axios 拦截器附加 token 并处理 401 重定向。
- **权限模型**：根据用户角色动态加载路由。`v-permission` 指令用于元素级别的权限控制。
- **增删改查页面**遵循统一模式：列表/搜索表单 → 分页表格 → 新增/编辑弹窗，通过 `Pagination` 和 `RightToolbar` 组件复用。

## 服务间通信

前端 → `/hejiayun/*` → hjy-community（8080）
前端 → `/ai/*` → hjy-ai-service（8090）
hjy-ai-service → `/system/*`、`/community/*` 等 → hjy-community（8080），通过 `HjyCommunityClient` 调用

## 数据库

两个服务共享同一个 MySQL 数据库。主要表：
- `sys_*` —— 系统管理（用户、角色、菜单、部门、字典、配置、通知）
- `hjy_*` —— 物业业务（业主、楼栋、单元、房间、报修、投诉、访客）
- AI 知识库相关表由 hjy-ai-service 管理

## 提交风格

中文提交信息，以功能描述为主。
