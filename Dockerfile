# 1단계: 빌드
FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# 2단계: 실행
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENV TZ=Asia/Seoul
ENTRYPOINT ["java", "-jar", "app.jar"]
