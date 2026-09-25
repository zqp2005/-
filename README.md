# 合家云社区物业管理平台

合家云是用于毕设演示的社区物业管理系统，由管理后台、居民微信小程序、主业务服务、AI 助手和 MCP 工具服务组成。当前重点是居民建档与开通、人房关联、报修派单、投诉处理和权限内查询；收费、消息中心、居民验收返工等商业化能力尚未实现。

## 项目业务逻辑

1. **建立基础资料**：管理员维护小区、楼栋、单元和房间。下级资料必须归属真实父级；存在下级数据时，删除会被拒绝。房屋关联不等于实际入住，不自动改变房间入住状态。
2. **建立居民身份**：新手机号可在小程序注册。物业也可先在网页端建立居民档案，线下核验身份与手机号后发放 15 分钟有效的一次性激活码。居民在小程序输入激活码，核对档案姓名和脱敏手机号，只设置密码即可开通原档案，不产生第二条居民记录。
3. **申请房屋关联**：管理端或居民端提交人房关联，物业审核通过或驳回；审核、撤回与解绑留痕。该流程只证明系统内的关联审核，不等同于产权或入住登记。
4. **处理报修**：居民提交报修，生成 RX 工单号；客服派给有接单权限的启用人员，受派维修人员接单并完成。非法跳状态、重复处理和非受派人处理会被拒绝。居民查看本人提交的新工单进度。
5. **处理投诉建议**：居民提交投诉或建议，物业受理、回复处理结果并关闭；不予受理需要原因。居民查看本人提交的记录。
6. **辅助查询**：管理端物业工作台汇总待审核、待派单和待处理事项。AI 在当前登录账号权限内查询真实报修、投诉、公告和社区资料；创建报修需用户二次确认。未接入的账单、车辆、设施等数据不生成虚构结果。

主要状态流转：

| 业务 | 当前流程 |
| --- | --- |
| 人房关联 | 审核中 → 已绑定或已驳回；审核中可撤回，已绑定可解绑 |
| 报修 | 待处理 → 已分派 → 处理中 → 已处理；待处理可取消或拒绝，已分派可取消 |
| 投诉建议 | 待受理 → 处理中 → 已回复 → 已关闭；待受理可带原因关闭 |

完整演示步骤、反例与测试命令见 [毕设核心业务与测试指南](doc/毕设核心业务与测试指南.md)；答辩常见问题与参考回答见 [毕设答辩问题与参考答案](doc/毕设答辩问题与参考答案.md)。

## 技术栈

| 模块 | 主要技术 |
| --- | --- |
| 主业务后端 | Java 8、Spring Boot 2.7.8、Spring MVC、Spring Security、JWT、MyBatis-Plus 3.4.1、PageHelper、Druid、MySQL 8、Redis、Maven |
| 后端辅助能力 | Spring AOP 操作审计、EasyPOI 导出、Orika 对象映射、Swagger 2、JUnit 5 与 Mockito |
| AI 服务 | Java 17、Spring Boot 3.2.5、Spring AI 1.0.0、DeepSeek `deepseek-chat`、Spring WebFlux/SSE、Redis、MCP Client |
| MCP 服务 | Java 17、Spring Boot 3.2.5、Spring AI MCP Server、SSE、高德地图 Web 服务 API |
| 管理前端 | Vue 2.7、Vite 5、Element UI 2.14、Vue Router、Vuex、Axios、Sass、ECharts、Quill |
| 居民端 | 原生微信小程序（WXML、WXSS、JavaScript、`wx.request`） |

前端和小程序是独立目录，不包含在本 Git 仓库中。根目录 `package.json` 仅服务于项目介绍 PPT 辅助脚本，不是管理前端依赖。

## 项目架构结构

```text
D:\Uilted\hjy\                         本仓库
├── hjy-community/                     主业务 API，端口 8080
│   ├── src/main/java/.../web/controller/  管理端与 /app 居民端接口
│   ├── src/main/java/.../{system,community,property}/  业务领域
│   ├── src/main/java/.../{framework,common}/  认证、配置与通用能力
│   ├── src/main/resources/mapper/      MyBatis XML
│   └── sql/                            初始化与业务流程脚本
├── hjy-ai-service/                    AI 对话 API，端口 8090
│   └── src/main/java/com/msb/hjy/ai/  controller、service、tools、client、config
├── hjy-mcp-server/                    登录位置与天气工具，端口 8091
└── doc/                               业务规则、测试指南

D:\Uilted\hjy_ui\hejiayun_ui\           Vue 管理前端，开发端口 80
D:\Uilted\hij_wweixin\                 微信小程序居民端
```

```text
管理前端 ── /hejiayun/* ──> hjy-community ──> MySQL + Redis
         └─ /ai/* ───────> hjy-ai-service ──> DeepSeek
                                      ├──当前用户 JWT──> hjy-community
                                      ├──会话记忆──> Redis
                                      └──MCP/SSE──> hjy-mcp-server ──> 高德 API
居民小程序 ── /app/* ────> hjy-community
```

数据库名为 `hehjiayun_community`（按项目现有拼写）；`sys_*` 表存系统管理数据，`hjy_*` 表存物业业务数据。旧 `chat_memory` 库不是当前 AI 会话存储，当前会话存在 Redis。

## 技术实现

### 认证与权限

- 管理端通过 Spring Security + JWT + Redis 登录态认证，方法级 `@PreAuthorize` 校验权限；前端动态路由和 `v-hasPermi` 仅控制展示，不能代替后端授权。
- 小程序 `/app/**` 使用独立业主 JWT 与 `owner_tokens:{uuid}`，不与管理端 `login_tokens:{uuid}` 通用。新工单用服务端认证得到的 `owner:居民ID` 标识本人，不能靠姓名或手机号认领历史单。
- AI 仅透传当前管理端用户令牌，不使用共享管理员账号。管理端 Druid 入口要求管理员登录及独立 Druid 凭据；导航中的“数据监控”菜单已移除。

### 居民档案与一次性激活

- 管理端发码时核对档案处于启用、存在手机号且未设小程序密码。随机激活码只展示一次，Redis 仅保存摘要与档案 ID 的短期索引，15 分钟过期；再次发码使旧码失效。
- 新建业主档案未指定状态时默认写入 `Enable`；显式停用的档案保持 `Disable`。历史空状态档案不批量启用，须逐条核验后处理。
- 2026-09-25 本地核验并修复了 1 条空状态、未开通的居民档案；条件更新仅影响该记录，复查为 `Enable`。代码定向测试 15 项通过；未执行云端数据修复，部署新建档案默认值修复需重启主后端。
- 居民手机号在服务层拒绝重复，查询发现历史重复时返回明确错误；数据库唯一索引负责并发兜底。迁移脚本为 `hjy-community/sql/owner-phone-unique.sql`，应用前必须人工核对并清理重复档案，不能自动合并。2026-09-25 本地已核对一组重复：保留有房屋关联档案的手机号，清空无房屋关联测试档案的手机号（该测试档案暂不能用手机号登录），随后建立索引；云端数据库未处理。
- 小程序通过 `POST /app/activation/preview` 以激活码查询待开通档案，只回显姓名和脱敏手机号，不下发身份证号或密码。提交 `POST /app/register` 时只传激活码和新密码，后端重新校验并短时预留激活码，为原档案设置密码成功后再删除激活码。普通新手机号注册仍使用姓名、手机号和密码。
- 激活时先短暂锁定有效激活码，数据库写入成功后才删码；数据库写入失败会释放锁，原码在剩余有效期内可重试。重新发码与激活互斥，避免旧码在发新码的同时提交。
- 2026-09-25 本批验证：手机号冲突、激活码失败重试等定向测试 21 项及主后端完整测试 67 项通过；在独立 Redis 端口验证脚本时序后已停止临时实例。其他环境须先运行迁移脚本中的查重查询，人工核对并处理重复记录，再建立唯一索引并重启主后端；本批未改小程序。停用与激活同时发生的竞态尚未处理。
- 物业必须在线下核验并安全交付激活码；系统没有短信归属或远程实名核验。激活码是短期凭据，不应截图公开传播。

### 状态、并发与审计

- 报修、投诉和房屋关联使用专用动作接口流转；普通编辑不能修改状态、处理人和操作时间。关键动作以原状态条件更新，冲突返回业务错误。
- 层级资料写入校验父级，删除前检查下级；关联审核、解绑与撤回写审核记录，登录与业务写操作记审计日志。
- 报修 RX 编号由雪花 ID 生成。多实例工作节点规划和数据库唯一约束仍需另行设计。

### AI 对话

- Spring AI `@Tool` 将报修、投诉、公告、居民、社区等真实接口接入 DeepSeek；未知或未接入数据明确说明。报修创建先在 Redis 保留待确认草稿，用户确认后才按当前账号权限提交。
- Redis 保存按稳定用户 ID 和会话 ID 隔离的消息窗口，重新登录可继续；“清空页面”只清本地展示，“彻底忘记”才删服务端记忆。同步与流式接口分别为 `POST /ai/chat`、`POST /ai/chat/stream`，流式输出使用 SSE。
- MCP 服务提供当前登录地址定位和高德天气工具；AI 服务默认启动时连接 8091，不使用 MCP 时可设置 `MCP_CLIENT_ENABLED=false`。

## 本地运行与验证

1. 准备 MySQL 8、Redis、Maven、Node.js 18+，主后端用 JDK 8，AI/MCP 用 JDK 17。按各服务 `src/main/resources/application-local.yml.template` 创建本地配置，填入数据库、JWT 与高德 Key；`DEEPSEEK_API_KEY` 通过环境变量提供。真实凭据不要提交。
2. 创建 `hehjiayun_community` 并按 `hjy-community/sql/` 的说明初始化。不要直接执行 `sql/backup/` 或未核验的数据修复脚本。
3. 先启动 Redis 和 MySQL，再分别在独立终端启动下列服务（AI 要在 MCP 之后启动）；小程序按 `config.js` 配置后端地址。真机不能使用电脑的 `localhost`。

```powershell
cd D:\Uilted\hjy\hjy-community; mvn spring-boot:run
cd D:\Uilted\hjy\hjy-mcp-server; mvn spring-boot:run
cd D:\Uilted\hjy\hjy-ai-service; mvn spring-boot:run
cd D:\Uilted\hjy_ui\hejiayun_ui; npm run dev
```

主后端定向测试可运行 `mvn "-Dtest=OwnerActivationServiceTest,OwnerAccountSecurityTest" test`，AI 服务运行 `mvn test`，前端运行 `node --test tests/sse.test.mjs tests/pattern.test.mjs` 与 `npm run build:prod`。真实注册、激活、派单和审核仍需用专门测试账号做浏览器及微信开发者工具端到端验收。测试结束应关闭自己启动的服务，不停止他人原有进程。

本批激活流程验证：主后端 13 项定向 Mock 测试通过，小程序注册脚本通过 `node --check`；未启动业务服务、未调用真实激活码，也未完成微信开发者工具端到端验收。部署时需同时更新主后端和小程序代码，新发的激活码才支持只凭码预览档案；本批无需数据库迁移。

## 当前边界

本项目以毕设核心流程为目标，尚无短信核验、社区级行权限、独立入住登记、真实物业账单与支付、居民验收返工和消息中心。旧工单不按姓名或电话自动归属居民；仅提交代码不代表运行中的服务已经更新。详细规则见 [业务规则与分批实施基线](doc/业务规则与分批实施基线.md)。
