# 合家云社区 AI 智能服务

基于 Spring AI 实现的社区物业智能助手服务。

## 技术栈

- **Java 17**
- **Spring Boot 3.2.5**
- **Spring AI 1.0.0-M6**
- **Spring AI Alibaba (通义千问)**
- **Redis** - 会话存储
- **MySQL** - 知识库存储

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
│   │   └── KnowledgeController.java     # 知识库API
│   ├── service/                        # 服务层
│   │   ├── ChatService.java
│   │   └── KnowledgeService.java
│   ├── agent/                          # AI智能体
│   │   ├── HjyAgent.java               # 智能体定义
│   │   └── SystemPrompt.java           # 系统提示词
│   ├── tools/                          # 工具集
│   │   ├── RepairTool.java             # 报修工具
│   │   ├── ComplaintTool.java         # 投诉工具
│   │   ├── PropertyFeeTool.java        # 物业费工具
│   │   ├── OwnerInfoTool.java         # 业主信息工具
│   │   ├── AnnouncementTool.java      # 公告工具
│   │   └── CommunityTool.java         # 社区信息工具
│   ├── dto/                            # 数据传输对象
│   ├── common/                         # 公共模块
│   └── knowledge/                      # 知识库模块
└── src/main/resources/
    └── application.yml                 # 配置文件
```

## 功能特性

### 1. 智能客服
- 7x24小时在线服务
- 自然语言对话交互
- 多轮对话上下文理解

### 2. 智能工具调用
- 报修服务（查询、提交、进度追踪）
- 投诉处理（提交、查询、处理进度）
- 物业费查询（账单、缴费、欠费）
- 业主信息查询（基本信息、车辆、家庭成员）
- 社区公告（通知、活动、设施）

### 3. 知识库问答
- 基于 RAG 的知识检索
- 物业条例、常见问题、规章制度

### 4. 多场景Agent
- 物业客服助手
- 物业管理系统助手
- 数据分析助手

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.8+
- Redis 6+
- 阿里云 DashScope API Key

### 2. 配置

编辑 `application.yml`：

```yaml
hjy:
  ai:
    api-key: your-dashscope-api-key
    model: qwen-plus
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
  -e DASHSCOPE_API_KEY=your-api-key \
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

## 配置参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| hjy.ai.model | AI模型 | qwen-plus |
| hjy.ai.temperature | 温度参数 | 0.7 |
| hjy.ai.maxTokens | 最大令牌数 | 2000 |

## 常见问题

### Q: 如何更换AI模型？
A: 修改 `application.yml` 中的 `hjy.ai.model` 配置项。

### Q: 支持哪些AI模型？
A: 通义千问系列（qwen-plus, qwen-turbo, qwen-max 等）

### Q: 如何接入现有数据库？
A: 在工具类中调用现有系统的 REST API 或直接操作数据库。

## License

MIT License
