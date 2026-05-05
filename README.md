# 合家云社区物业管理平台 (hjy-community)

基于 Spring Boot + Vue 的模块化社区物业管理平台，集成 AI 智能客服助手。

## 项目架构

```
hejiayun_ui (Vue 2 + Element UI, 端口 80)
    │
    ├── /hejiayun/* ──→ hjy-community (Spring Boot 2.7, 端口 8080)
    │                        │
    │                        ├── MySQL + Redis + MyBatis-Plus
    │                        └── Spring Security + JWT 认证
    │
    └── /ai/* ────────→ hjy-ai-service (Spring Boot 3.2, 端口 8090)
                             │
                             ├── DeepSeek API (Spring AI + Function Calling)
                             ├── Redis (会话存储)
                             └── HTTP ──→ hjy-community (数据查询)
```

## 技术栈

| 层级 | 技术 |
|------|------|
| **后端框架** | Spring Boot 2.7.8 / 3.2.5、Spring Security |
| **AI 框架** | Spring AI 1.0.0 + DeepSeek + Function Calling |
| **持久层** | MyBatis-Plus 3.4.1、PageHelper、MySQL 8.0 |
| **缓存** | Redis (Jedis / Lettuce) |
| **前端** | Vue 2.6 + Element UI 2.14 + Vuex + Vue Router |
| **工具库** | EasyPOI、Orika、FastJSON、Hutool、Druid、Lombok |
| **认证** | JWT Token + BCrypt + Spring Security |

## 模块划分

### hjy-community（主后端）

```
com.msb.hjycommunity
├── common/          # 公共基础（BaseController、BaseEntity、统一响应、异常处理、工具类）
├── framework/       # 框架配置（Security、Redis、Swagger、Druid、JWT过滤器）
├── system/          # 系统管理（用户、角色、菜单、部门、字典、通知、配置）
├── community/       # 社区管理（社区基本信息CRUD）
├── property/        # 物业业务（业主、楼栋、单元、房间、报修、投诉、访客）
├── monitor/         # 系统监控（定时任务、操作日志、登录日志、在线用户）
└── web/controller/  # REST 控制器层
```

### hjy-ai-service（AI 智能客服）

```
com.msb.hjy.ai
├── controller/      # ChatController（对话API、流式SSE）
├── service/         # ChatService（对话逻辑、会话管理）
├── agent/           # AI代理（HjyAgent、SystemPrompt、ToolExecutor）
├── tools/           # 6大工具集（RepairTool、ComplaintTool、PropertyFeeTool等）
├── client/          # HjyCommunityClient（调用主后端HTTP接口）
├── config/          # ChatClient配置、Redis、Web、AI参数
├── prompt/          # 提示词模板管理
├── dto/             # 数据传输对象
├── model/           # 领域模型
└── common/          # 常量、异常、统一结果封装
```

## 核心功能

### 物业管理

| 模块 | 功能 |
|------|------|
| **小区管理** | 社区信息维护、多条件分页查询 |
| **楼栋/单元/房间** | 房产档案管理，层级关联 |
| **业主管理** | 业主信息登记、车辆绑定、家庭成员 |
| **报修服务** | 报修提交、工单流转（待处理→已派单→处理中→已完成→已评价）、取消、评价 |
| **投诉建议** | 投诉提交、状态跟踪、处理评价 |
| **访客管理** | 访客登记、来访记录查询 |
| **公告通知** | 社区公告、活动发布管理 |

### 系统管理

| 模块 | 功能 |
|------|------|
| **用户管理** | 用户增删改查、状态管理、密码重置 |
| **角色管理** | 角色分配、权限绑定 |
| **菜单管理** | 动态路由配置、按钮级权限 |
| **部门管理** | 组织架构树形管理 |
| **数据字典** | 字典类型/数据维护 |

### AI 智能客服

| 模块 | 能力 |
|------|------|
| **报修查询** | "查报修进度" → 自动调接口返回工单列表 |
| **报修提交** | "我家水管漏水" → AI 提取信息创建工单 |
| **物业费查询** | "物业费多少" → 查询业主信息计算费用 |
| **投诉建议** | "我要投诉噪音" → 自动提交投诉工单 |
| **公告查询** | "最近有什么通知" → 查询公告列表 |
| **社区信息** | "小区有什么设施" → 返回设施和周边配套 |
| **业主信息** | "我的车辆信息" → 查询业主和车辆数据 |

## 快速开始

### 环境要求

- JDK 8+（hjy-community）/ JDK 17+（hjy-ai-service）
- Maven 3.8+
- MySQL 8.0
- Redis 6+
- Node.js ≥ 8.9（前端）
- DeepSeek API Key（AI 服务）

### 1. 启动主后端

```bash
cd hjy-community
# 创建数据库 hehjiayun_community，执行初始化SQL
# 修改 application-druid.yml 中的数据库连接信息
mvn spring-boot:run
# 启动于 http://localhost:8080
```

### 2. 启动 AI 服务

```bash
cd hjy-ai-service
# 设置环境变量 DEEPSEEK_API_KEY
mvn spring-boot:run
# 启动于 http://localhost:8090
```

### 3. 启动前端

```bash
cd hejiayun_ui
npm install
npm run dev
# 启动于 http://localhost:80
```

### Docker 部署（AI 服务）

```bash
cd hjy-ai-service
docker build -t hjy-ai-service .
docker run -d -p 8090:8090 -e DEEPSEEK_API_KEY=your-key hjy-ai-service
```

## API 文档

### 对话接口

```http
POST /ai/chat
Content-Type: application/json

{
  "sessionId": "user-123",
  "message": "查一下我的物业费账单",
  "userId": 1,
  "userName": "张三"
}
```

### 流式对话

```http
POST /ai/chat/stream
Content-Type: application/json

{
  "sessionId": "user-123",
  "message": "最近有什么公告"
}
```

### AI 服务核心接口一览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/chat` | 同步对话 |
| POST | `/ai/chat/stream` | 流式对话（SSE） |
| DELETE | `/ai/session/{sessionId}` | 清除会话历史 |
| GET | `/ai/health` | 健康检查 |

## 安全机制

- **无状态认证**：基于 JWT Token，过滤器校验，30 分钟过期
- **密码加密**：BCrypt 加密存储
- **权限控制**：`@PreAuthorize` 方法级权限 + 前端 `v-permission` 按钮级控制
- **CORS**：前后端分离跨域支持
- **AI 服务**：通过 `/aiLogin` 免验证码接口认证，Token 自动缓存

## 设计亮点

1. **Function Calling 实现 AI 工具调用**：6 大类 20+ 工具函数，AI 自动识别意图并调用对应接口获取真实数据，避免幻觉
2. **分层架构**：Controller → Service → Mapper，基类封装通用逻辑，减少重复代码
3. **统一规范**：统一响应格式 `R<T>`、统一异常处理、统一分页封装
4. **会话隔离**：按 sessionId 隔离 AI 对话上下文，支持多用户并发对话
5. **流式输出**：WebFlux SSE 实现打字机效果，提升交互体验
6. **凭据安全**：敏感配置通过环境变量注入，不硬编码在配置文件中

## 项目结构总览

```
hjy/
├── hjy-community/          # 主物业管理后端 (Spring Boot 2.7 + Java 8)
│   ├── src/main/java/com/msb/hjycommunity/
│   └── src/main/resources/
├── hjy-ai-service/          # AI 智能客服 (Spring Boot 3.2 + Java 17)
│   ├── src/main/java/com/msb/hjy/ai/
│   └── src/main/resources/
├── hejiayun_ui/             # 管理后台前端 (Vue 2 + Element UI)
│   └── src/
├── CLAUDE.md                # 项目开发指南
└── README.md
```
