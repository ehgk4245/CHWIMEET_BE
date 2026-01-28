# Stage 1: Build
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Gradle 캐시 활용을 위해 설정 파일 먼저 복사
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

RUN chmod +x ./gradlew

# 의존성 먼저 다운로드 (코드 변경 시 빌드 속도 향상)
RUN ./gradlew dependencies --no-daemon

# 소스 복사 및 빌드
COPY src ./src
# 빌드 시 테스트는 제외하여 속도 향상 (CI에서 이미 검증했다면)
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

# 필수 패키지 및 타임존 설정 (KST)
RUN apk add --no-cache curl tzdata
ENV TZ=Asia/Seoul

WORKDIR /app

# 1. 로그 디렉토리 생성 및 권한 설정 (유저 생성 전 실행)
# root 권한으로 폴더를 만들고 spring 유저에게 소유권을 넘깁니다.
RUN mkdir -p /app/logs && \
    addgroup -S spring && \
    adduser -S spring -G spring && \
    chown -R spring:spring /app/logs

# 2. 빌드된 JAR 복사 (소유권을 spring으로 지정하며 복사)
COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

# 3. 보안을 위해 spring 유저로 전환
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", \
  "app.jar"]