# 前端静态托管 + API 反代（构建上下文放 dist/ 目录）
# 注意：nginx:alpine 最新版(1.31+)在 CentOS7 的 3.10 老内核上启动报
# "pwrite() nginx.pid Operation not permitted"，锁定 1.24 稳定版
FROM nginx:1.24-alpine
ENV TZ=Asia/Shanghai
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY dist/ /usr/share/nginx/html/
EXPOSE 80
