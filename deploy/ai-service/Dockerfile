# hjy-ai-service AI 服务（Java 17）
# 构建上下文放：app.jar、application-local.yml（服务器版：TOKEN_SECRET / DEEPSEEK_API_KEY）
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY app.jar app.jar
COPY application-local.yml /app/application-local.yml
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms128m -Xmx512m"
EXPOSE 8090
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar --spring.config.additional-location=file:/app/application-local.yml"]
