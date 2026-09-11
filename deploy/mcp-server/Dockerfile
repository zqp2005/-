# hjy-mcp-server MCP 工具服务（Java 17，仅内网访问，不映射宿主机端口）
# 构建上下文放：app.jar、application-local.yml（服务器版：MYSQL 凭据 / AMAP_KEY）
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY app.jar app.jar
COPY application-local.yml /app/application-local.yml
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms64m -Xmx256m"
EXPOSE 8091
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar --spring.config.additional-location=file:/app/application-local.yml"]
