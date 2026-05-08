# 合家云社区 AI 智能服务

基于 Spring AI 实现的社区物业智能助手服务。

## 技术栈

| 技术 | 说明 |
|------|------|
| Java 17 | 基础语言 |
| Spring Boot 3.2.5 | 基础框架 |
| Spring AI 1.0.0 | AI大模型集成框架 |
| DeepSeek | 大语言模型 |
| Spring WebFlux | 支持流式输出 |
| Redis | 会话/对话历史存储 |
| MySQL | 知识库存储 |
| Lombok | 简化代码 |

## 项目结构

```
hjy-ai-service/
├── src/main/java/com/msb/hjy/ai/
│   ├── HjyAiApplication.java          # 启动类
│   ├── config/                         # 配置类
│   │   ├── AiConfig.java               # AI配置
│   │   ├── ChatConfig.java             # 对话配置
│   │   ├── RedisConfig.java            # Redis配置
│   │   └── WebConfig.java              # Web配置
│   ├── controller/                     # 控制器层
│   │   ├── ChatController.java         # 对话API
│   │   └── KnowledgeController.java    # 知识库API
│   ├── service/                        # 服务层
│   │   ├── ChatService.java
│   │   └── KnowledgeService.java
│   ├── client/                         # HTTP客户端
│   │   └── HjyCommunityClient.java     # 调用社区系统API
│   ├── agent/                          # AI智能体
│   │   ├── HjyAgent.java               # 智能体定义
│   │   ├── ToolExecutor.java           # 工具执行器
│   │   └── SystemPrompt.java           # 系统提示词
│   ├── tools/                          # 工具集
│   │   ├── RepairTool.java             # 报修工具
│   │   ├── ComplaintTool.java          # 投诉工具
│   │   ├── PropertyFeeTool.java        # 物业费工具
│   │   ├── OwnerInfoTool.java          # 业主信息工具
│   │   ├── AnnouncementTool.java       # 公告工具
│   │   └── CommunityTool.java          # 社区信息工具
│   ├── dto/                            # 数据传输对象
│   ├── common/                         # 公共模块
│   └── knowledge/                      # 知识库模块
└── src/main/resources/
    └── application.yml                 # 配置文件
```

## 角色

目前只有 **1个角色**：AI物业客服助手"小合"

系统通过统一的 System Prompt 定义角色行为，没有多角色设计。

## 功能特性

### 1. 智能客服
- 7x24小时在线服务
- 自然语言对话交互
- 多轮对话上下文理解

### 2. 智能工具调用（6大类28个工具）

| 模块 | 功能 |
|------|------|
| **报修服务** | 查询进度、提交报修、详情查询、取消工单、评价 |
| **投诉建议** | 查询进度、提交投诉、详情查询、评价 |
| **物业缴费** | 查询账单、缴费指南、历史记录、欠费查询 |
| **业主服务** | 业主信息、车辆信息、家庭成员、访客登记 |
| **社区信息** | 社区基本信息、设施查询、周边配套、预约场地、便民服务 |
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

### 4. 知识库问答
- 基于 RAG 的知识检索
- 物业条例、常见问题、规章制度

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.8+
- Redis 6+
- DeepSeek API Key

### 2. 配置

编辑 `application.yml`：

```yaml
hjy:
  ai:
    api-key: your-deepseek-api-key
    model: deepseek-chat
```

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

### 对话接口

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

### 知识库管理

```http
# 添加知识
POST /ai/knowledge

# 搜索知识
GET /ai/knowledge/search?query=物业费标准&category=faq

# 重建索引
POST /ai/knowledge/rebuild/{category}
```

## 对接现有系统

在 `hjy-community` 项目中添加 AI 服务调用：

```java
@RestController
@RequestMapping("/ai")
public class AiIntegrationController {

    private final RestTemplate restTemplate;
    private final String aiServiceUrl = "http://127.0.0.1:8090";

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        String url = aiServiceUrl + "/ai/chat";
        return restTemplate.postForObject(url, request, Result.class);
    }
}
```

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
所有请求需要 `Authorization: Bearer {token}` header，通过 `/aiLogin` 获取token

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
| hjy.ai.hjy-community.admin-username | 管理员用户名 | admin |
| hjy.ai.hjy-community.admin-password | 管理员密码 | admin123 |

## 常见问题

### Q: 如何更换AI模型？
A: 修改 `application.yml` 中的 `hjy.ai.model` 配置项。

### Q: 支持哪些AI模型？
A: DeepSeek 系列（deepseek-chat, deepseek-coder 等）

### Q: 如何接入现有数据库？
A: 在工具类中调用现有系统的 REST API 或直接操作数据库。

## License

MIT License
