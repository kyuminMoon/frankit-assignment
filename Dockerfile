# 멀티 스테이지 빌드 - 빌드 단계
FROM eclipse-temurin:23-jdk-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# gradle 파일 복사 (레이어 캐싱 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (캐싱을 위한 별도 단계)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src src
RUN ./gradlew build -x test --no-daemon

# 실행 단계 - JRE만 포함하는 경량 이미지
FROM eclipse-temurin:23-jre-alpine

# 타임존 설정
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 실행 계정 생성
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 애플리케이션 디렉토리 생성 및 권한 설정
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# 실행 계정으로 전환
USER appuser

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일만 복사
COPY --from=build --chown=appuser:appgroup /app/build/libs/*.jar app.jar

# 로그 볼륨 설정
VOLUME ["/app/logs"]

# 애플리케이션 헬스체크를 위한 포트 노출
EXPOSE 8080

# JVM 메모리 및 GC 최적화 설정
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE="prod"

# 컨테이너 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# 앱 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]