# syntax=docker/dockerfile:1
# 构建上下文：qc-retirement/（Maven 父工程所在目录）
#   docker build -t zzyl:1.0 .
#
# ---------- 第一阶段：构建 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# 先只拷 pom，把「下载依赖」单独做一层缓存：
# 只要 pom 没变，改代码后重新构建不会重新下载依赖
COPY pom.xml ./
COPY qc-pojo/pom.xml    qc-pojo/
COPY qc-util/pom.xml    qc-util/
COPY qc-service/pom.xml qc-service/
RUN mvn -B -q -DskipTests dependency:go-offline

# 再拷源码并打包
COPY qc-pojo    qc-pojo
COPY qc-util    qc-util
COPY qc-service qc-service
RUN mvn -B -q -DskipTests package

# ---------- 第二阶段：运行 ----------
FROM eclipse-temurin:21-jre

ENV TZ=Asia/Shanghai \
    LANG=C.UTF-8 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Duser.timezone=Asia/Shanghai"

WORKDIR /app

# 健康检查要用 curl，官方 jre 镜像里没有，装一个（约 1 MB）
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

# 非 root 运行：单独建一个服务账号
RUN groupadd -r app && useradd -r -g app -s /sbin/nologin app

# 只把产物拷进运行镜像，源码、Maven、构建缓存都不进
COPY --from=build /build/qc-service/target/qc-service-*.jar /app/app.jar
RUN chown -R app:app /app
USER app

EXPOSE 9997

HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -fsS http://127.0.0.1:9997/actuator/health || exit 1

# ENTRYPOINT：固定不变的部分（java 命令 + jar）
#   sh -c 负责展开 $JAVA_OPTS；开头的 exec 让 java 顶替 sh 成为 PID 1，
#   否则 docker stop 发出的 SIGTERM 到不了 JVM，优雅停机会失效
# CMD：默认参数，docker run 时可在镜像名后面整体覆盖
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar \"$@\"", "sh"]
CMD ["--spring.profiles.active=prod"]
