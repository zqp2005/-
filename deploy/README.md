# deploy/ —— 云服务器部署物料

部署目标：阿里云 ECS（CentOS 7, 2C/1.7G），复用服务器上已有的 `mysql:8.0` 与 `redis:5.0.9` 容器。

## 目录说明

| 内容 | 说明 |
|------|------|
| `docker-compose.yml` | 四个服务的编排（community / mcp / ai / frontend） |
| `community/Dockerfile` | eclipse-temurin:8-jre，Xmx400m |
| `ai-service/Dockerfile` | eclipse-temurin:17-jre，Xmx512m |
| `mcp-server/Dockerfile` | eclipse-temurin:17-jre，Xmx256m，仅内网 |
| `frontend/Dockerfile` + `nginx.conf` | nginx:alpine 托管 dist，`/prod-api` → 8080、`/ai` → 8090（SSE 关缓冲） |
| `sql/` | 本地库 dump（**不入库**，gitignore） |

## 环境变量（容器内注入，代码里都有默认值不传则本地行为）

| 变量 | 用在哪 | 容器里传 |
|------|--------|----------|
| `MYSQL_HOST` | community / mcp 的数据源地址 | `mysql` |
| `REDIS_HOST` | community / ai 的 Redis 地址 | `redis` |
| `MCP_HOST` | ai 连 MCP 服务的地址 | `mcp-server` |
| `COMMUNITY_HOST` | ai 调主后端的地址 | `community` |

## 敏感配置

各服务的 `application-local.yml`（MySQL 密码 / TOKEN_SECRET / DEEPSEEK_API_KEY / AMAP_KEY）**不入仓库**，
部署时从本地开发目录 scp 到服务器对应 build 上下文，经 `--spring.config.additional-location` 加载。

## 部署步骤摘要

1. 本地构建：三个 `mvn clean package -DskipTests` + 前端 `vue-cli-service build`
2. 本地导库：`mysqldump ... --databases hehjiayun_community --add-drop-database | gzip > sql/hehjiayun.sql.gz`
3. scp 上传 jar（改名 app.jar）、dist/、本目录、三个 application-local.yml
4. 服务器：建 `hjy-net` 网络并把 mysql/redis 接入；导入 SQL
5. `docker compose up -d --build`
6. 验证：80 前端 / 8080 captchaImage / 8090 ai health
