# 前端静态托管 + API 反代（构建上下文放 dist/ 目录）
FROM nginx:alpine
ENV TZ=Asia/Shanghai
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY dist/ /usr/share/nginx/html/
EXPOSE 80
