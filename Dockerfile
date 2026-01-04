FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Инсталираме Maven
RUN apk add --no-cache maven

# Копираме pom.xml
COPY pom.xml ./

# Изтегляме dependencies
RUN mvn dependency:go-offline -B

# Копираме source кода
COPY src ./src

# Build на приложението
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копираме jar файла от builder stage
COPY --from=builder /app/target/*.jar app.jar

# Експозваме порта
EXPOSE 8080

# Конфигурация на JVM - УВЕЛИЧЕНА ПАМЕТ ЗА IMPORT
ENV JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:MaxMetaspaceSize=256m"

# Стартираме приложението с JVM опции
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]