# hjy-community 主后端（Java 8）
# 构建上下文放：app.jar（由本地 target/*.jar 复制改名）、application-local.yml（服务器版真实凭据）
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY app.jar app.jar
COPY application-local.yml /app/application-local.yml
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms128m -Xmx400m"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar --spring.config.additional-location=file:/app/application-local.yml"]
