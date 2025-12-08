# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM eclipse-temurin:25-jdk AS builder

# 이 이미지는 이미 JDK 25가 들어있어서 별도 설치 불필요
# (원래 있던 apt + wget + tar 블록 완전 삭제)

# 필요하면 JAVA_HOME만 이미지 기본값으로 맞춰주기 (선택)
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="$JAVA_HOME/bin:${PATH}"

WORKDIR /app

# gradle wrapper와 설정 먼저 복사
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

# 혹시 모를 권한 문제 방지
RUN chmod +x ./gradlew

# 의존성 다운로드 (캐시용 레이어)
RUN ./gradlew dependencies --no-daemon

# 소스 복사
COPY src ./src

# 애플리케이션 빌드
RUN ./gradlew bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# curl 없으면 HEALTHCHECK에서 바로 죽으니까 추가 설치
RUN apk add --no-cache curl

# 로그 디렉토리 미리 생성
RUN mkdir -p /app/logs && chmod 755 /app/logs

# non-root 유저
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]