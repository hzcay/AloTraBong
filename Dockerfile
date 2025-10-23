# --- Stage 1: Build ---
FROM maven:3.9-amazoncorretto-17 AS builder
WORKDIR /app

# Copy file pom để preload dependency
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime ---
FROM amazoncorretto:17-alpine
WORKDIR /app

# Cài font hỗ trợ tiếng Việt và emoji (Excel, PDF, UI)
RUN apk add --no-cache \
    fontconfig \
    ttf-dejavu \
    && fc-cache -f

# Copy jar từ builder
COPY --from=builder /app/target/*.jar app.jar

# Cấu hình runtime
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]