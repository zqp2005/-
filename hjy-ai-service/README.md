# 合家云社区 AI 智能服务

## 毕设精简范围与本批修复

- AI 是已登录管理人员的辅助查询入口，不是替代业务页面的自动执行人。不扩展复杂审批、收费或消息功能。
- 社区仅返回真实名称/详细地址；公告按实际类型 1/2、发布状态和标题查询；业主检索条件送后端，展示电话掩码。车辆、家庭关系、设施、便民服务和按居民访客核验未接入，明确告知不可查询。
- 报修同时区分 RX 工单号与数据库记录 ID；RX 号先查询完全匹配项，不把业务编号直接当数字主键。
- SSE 使用原生 ServerSentEvent 与 UTF-8 字符串转换；前端须同步升级，移除旧版重复 data 前缀兼容逻辑。清会话接口仍按当前账号隔离。
- 本批自动测试共 16 项通过，含 MockMvc 实际中文/多行 SSE 编码及工具真实字段测试；未调用付费模型/天气接口，未宣称完成真实模型端到端验收。

## 2026-09-24 安全边界更新（以本节为准）

- 不再使用管理员自动登录或共享 API Token；工具请求只透传当前已验证用户令牌，权限不足直接失败，不重登重试。
- 服务端 ToolContext 只用于本地执行身份，不进入模型 JSON 参数，也不传播到远端 MCP。调用结束/异常后清除线程上下文。
- 新增/取消等写操作在确认机制完成前拒绝，用户通过管理端/小程序业务页面提交；登录定位工具暂停，天气仍可查询。
- 费用工具未接入账单，明确返回暂不可查询，不给出虚构欠费、费率、联系电话或支付入口。报修/投诉列表按后端字段服务端筛选后分页。
- 与主后端配套部署：`/aiLogin` 已关闭，旧会话需重新登录；AI 入口调用主后端 `/getInfo` 验证账号实时状态。
- 验证：`mvn test` 10 项通过（JWT 解析 5 项、工具授权/身份隔离/费用真实性 5 项），未调用外部模型、未写数据库。
- 下文旧功能示例不代表写操作、真实收费、车辆/设施等未接入模块已开放；权限不足和未接入必须如实提示。

基于 Spring AI 实现的社区物业智能助手服务，通过工具调用（Function Calling）对接 `hjy-community` 主后端获取真实业务数据，为业主提供智能客服对话。

## 技术栈

| 技术 | 说明 |
|------|------|
| Java 17 | 基础语言 |
| Spring Boot 3.2.5 | 基础框架 |
| Spring AI 1.0.0 | AI大模型集成框架（DeepSeek Starter） |
| DeepSeek | 大语言模型（deepseek-chat） |
| Spring WebFlux | 支持流式输出（SSE） |
| Redis | 会话记忆持久化存储 |
| Lombok | 简化代码 |

## 项目结构

```
hjy-ai-service/
├── src/main/java/com/msb/hjy/ai/
│   ├── HjyAiApplication.java          # 启动类
│   ├── config/                         # 配置类
│   │   ├── AiConfig.java               # AI配置（RestTemplate 等）
│   │   ├── AiProperties.java           # hjy.ai.* 配置属性映射
│   │   ├── ChatClientConfig.java       # ChatClient/ChatMemory Bean + 系统提示词加载
│   │   ├── RedisChatMemoryRepository.java  # 会话记忆 Redis 存储
│   │   ├── RedisConfig.java            # Redis配置
│   │   └── WebConfig.java              # Web配置
│   ├── controller/                     # 控制器层
│   │   └── ChatController.java         # 对话API（同步/流式/清会话/健康检查）
│   ├── service/                        # 服务层
│   │   ├── ChatService.java            # 对话服务接口
│   │   └── impl/ChatServiceImpl.java   # 对话实现（含问候/帮助拦截）
│   ├── client/                         # HTTP客户端
│   │   └── HjyCommunityClient.java     # 透传当前调用者令牌，不自动登录
│   ├── tools/                          # 工具集（@Tool 注解，由 AI 自动调用）
│   │   ├── RepairTool.java             # 报修工具
│   │   ├── ComplaintTool.java          # 投诉工具
│   │   ├── PropertyFeeTool.java        # 物业费工具
│   │   ├── OwnerInfoTool.java          # 业主信息工具
│   │   ├── AnnouncementTool.java       # 公告工具
│   │   └── CommunityTool.java          # 社区信息工具
│   ├── prompt/                         # 提示词模板
│   │   └── PromptTemplate.java         # 问候语/帮助信息等常量模板
│   ├── dto/                            # 数据传输对象
│   │   ├── ChatRequest.java
│   │   └── ChatResponse.java
│   └── common/                         # 公共模块
│       ├── constant/                   # 常量（AiConstants、RedisKeys）
│       ├── exception/                  # 全局异常处理
│       └── result/                     # 统一响应封装 Result
└── src/main/resources/
    ├── application.yml                 # 配置文件
    └── prompt/system-prompt.txt        # 系统提示词（外置文件）
```

## 角色

目前只有 **1个角色**：AI物业客服助手"小合"

系统提示词外置于 `src/main/resources/prompt/system-prompt.txt`，由 `ChatClientConfig` 启动时加载，强制 AI 在物业相关问题上必须调用工具获取真实数据（避免幻觉）。没有多角色设计。

## 功能特性

### 1. 智能客服
- 7x24小时在线服务
- 自然语言对话交互（同步 + SSE 流式打字机效果）
- 多轮对话上下文理解（Redis 会话记忆）
- 问候语 / "帮助" 等常见指令本地拦截，无需调用大模型

### 2. 智能工具调用（6大类28个工具）

| 模块 | 功能 |
|------|------|
| **报修服务** | 查询进度、提交报修、详情查询、取消工单、评价 |
| **投诉建议** | 查询进度、提交投诉、详情查询、评价 |
| **物业缴费** | 查询账单、缴费指南、历史记录、欠费查询、费用试算 |
| **业主服务** | 业主信息、车辆信息、家庭成员、访客查询、访客登记 |
| **社区信息** | 社区基本信息、设施查询、周边配套、预约场地、门禁卡、便民服务 |
| **公告活动** | 公告列表、详情查询、社区活动 |

### 3. 可回答的问题示例

#### 报修服务
- 查报修进度、我的报修
- 我要报修、提交报修
- 报修详情、工单号xxx
- 取消报修、撤销工单
- 评价报修

#### 投诉建议
- 投诉进度、我的建议
- 我要投诉、提建议
- 投诉详情、建议详情
- 评价投诉

#### 物业缴费
- 物业费、账单查询
- 如何缴费、缴费方式
- 历史缴费、以往缴费
- 有没有欠费、欠费多少

#### 业主服务
- 我的信息、业主信息
- 车辆信息、车牌号
- 家庭成员、家人信息
- 访客、访客记录
- 登记访客、有人来访

#### 社区信息
- 小区介绍、社区信息、小区怎么样
- 有什么设施、健身房、游乐场
- 周边有什么、附近配套、地铁站、学校
- 预约场地、预约设施、预定篮球场
- 门禁卡、门禁权限
- 便民服务、维修、家政
- 公告、通知、最新消息
- 社区活动

### 4. 核心机制

- **系统提示词外置**：提示词文本在 `resources/prompt/system-prompt.txt`，修改提示词无需改代码
- **Redis 会话持久化**：`RedisChatMemoryRepository` 将每个会话存为 Redis List（key 前缀 `ai:chat:memory:`），配合 `MessageWindowChatMemory` 保留最近 20 条消息，服务重启后历史不丢、多实例可共享
- **Token 隔离**：`HjyCommunityClient` 透传当前用户 JWT；401/403 或业务失败直接拒绝，不共享管理员凭据，不自动重试写请求
- **SSE 流式输出**：流式接口逐段输出 `data: {...}\n\n`，正常结束以 `data: [DONE]` 标记收尾，前端据此结束加载状态
- **MCP 远程工具**：通过 `spring-ai-starter-mcp-client-webflux` 以 SSE 连接 `hjy-mcp-server`（默认 `http://127.0.0.1:8091`，见 `spring.ai.mcp.client.sse.connections.hjy-context`），自动注册远程工具 `query_login_location`（登录地址定位）和 `query_weather`（高德天气），与本地六大工具同时生效

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.8+
- Redis 6+
- DeepSeek API Key
- hjy-community 主后端运行中（默认 `http://localhost:8080`）
- **hjy-mcp-server 先于本服务启动**（默认 `http://127.0.0.1:8091`）：MCP 客户端在启动时建立 SSE 连接，server 未启动会导致本服务启动失败（约 20 秒超时报错）。确认不需要 MCP 工具时，可设环境变量 `MCP_CLIENT_ENABLED=false` 跳过连接（本服务仍可正常启动，仅缺少登录定位/天气两个工具）

### 2. 配置

API Key 通过环境变量注入：

```bash
export DEEPSEEK_API_KEY=your-deepseek-api-key
```

无需配置 `HJY_COMMUNITY_ADMIN_USER`、`HJY_COMMUNITY_ADMIN_PASSWORD` 或共享 API Token，旧值已不再使用。

### 3. 启动

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/hjy-ai-service-1.0.0.jar
```

### 4. Docker 部署

```bash
docker build -t hjy-ai-service .
docker run -d -p 8090:8090 \
  -e DEEPSEEK_API_KEY=your-api-key \
  hjy-ai-service
```

## API 文档

### 接口列表

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 同步对话 | POST | `/ai/chat` | 返回完整回复（Result\<ChatResponse\>） |
| 流式对话 | POST | `/ai/chat/stream` | SSE 逐段输出，以 `data: [DONE]` 结束 |
| 清除会话 | DELETE | `/ai/session/{sessionId}` | 清空该会话的 Redis 记忆 |
| 健康检查 | GET | `/ai/health` | 服务存活探测 |

### 请求示例

```http
POST /ai/chat
Content-Type: application/json

{
  "sessionId": "user-123",
  "message": "我想查询一下我的物业费账单",
  "agentType": "customer_service",
  "userId": 1,
  "userName": "张三"
}
```

其中 `sessionId`、`message` 必填；`agentType` 默认 `customer_service`；`userId`/`userName` 用于让 AI 识别用户身份。

### 流式响应格式

```
data: 您好

data: ，为您查询到以下账单...

data: [DONE]
```

## 与其他系统的对接

- **前端 → 本服务**：前端开发服务器将 `/ai/*` 代理到本服务（8090），前端直接调用上述对话接口。
- **本服务 → hjy-community**：`HjyCommunityClient` 透传调用者 JWT；主后端校验实时身份及操作权限。写操作暂不开放。

## 工具说明

| 工具 | 功能 | 调用场景 |
|------|------|----------|
| repairTool | 报修工单 | 查询报修进度、创建报修 |
| complaintTool | 投诉处理 | 提交投诉、查询处理 |
| propertyFeeTool | 物业费 | 查询账单、缴费指引 |
| ownerInfoTool | 业主信息 | 查询业主信息、车辆 |
| announcementTool | 社区公告 | 查询通知、活动 |
| communityTool | 社区信息 | 查询设施、周边配套 |

## 后端API接口

### 认证方式
所有业务请求需要 `Authorization: Bearer {token}`，使用管理端正常验证码登录获得的令牌；`/aiLogin` 已关闭。

### 报修服务
| 接口 | 方法 | 路径 |
|------|------|------|
| 查询报修列表 | GET | `/system/repair/list` |
| 提交报修 | POST | `/system/repair` |
| 报修详情 | GET | `/system/repair/{id}` |
| 取消/更新报修 | PUT | `/system/repair` |

### 投诉建议
| 接口 | 方法 | 路径 |
|------|------|------|
| 查询投诉列表 | GET | `/system/suggest/list` |
| 提交投诉 | POST | `/system/suggest` |
| 投诉详情 | GET | `/system/suggest/{id}` |

### 物业缴费/业主信息
| 接口 | 方法 | 路径 |
|------|------|------|
| 查询业主列表 | GET | `/system/owner/list` |

### 公告
| 接口 | 方法 | 路径 |
|------|------|------|
| 查询公告列表 | GET | `/system/notice/list` |

### 社区信息
| 接口 | 方法 | 路径 |
|------|------|------|
| 社区基本信息 | GET | `/system/community/list` |
| 周边配套 | GET | `/system/community/nearby` |
| 预约设施 | POST | `/system/facility/reserve` |

### 访客
| 接口 | 方法 | 路径 |
|------|------|------|
| 查询访客记录 | GET | `/system/visitor/list` |
| 登记访客 | POST | `/system/visitor` |

## 配置参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| hjy.ai.model | AI模型 | deepseek-chat |
| hjy.ai.temperature | 温度参数 | 0.7 |
| hjy.ai.maxTokens | 最大令牌数 | 2000 |
| hjy.ai.hjy-community.base-url | 社区系统地址 | http://localhost:8080 |
| 调用者认证 | 透传请求身份，不再配置服务管理员或固定 API Token | 必须正常登录 |

## 常见问题

### Q: 如何更换AI模型？
A: 修改 `application.yml` 中的 `hjy.ai.model` 配置项。

### Q: 支持哪些AI模型？
A: DeepSeek 系列（deepseek-chat, deepseek-coder 等）

### Q: 如何修改小合的角色设定/回复风格？
A: 编辑 `src/main/resources/prompt/system-prompt.txt` 后重启服务。

### Q: 会话历史存在哪里？
A: Redis（key 前缀 `ai:chat:memory:`），每个会话一个 List，保留最近 20 条消息，重启不丢失。

## License

MIT License
