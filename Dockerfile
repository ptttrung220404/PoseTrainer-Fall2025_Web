FROM eclipse-temurin:17-jdk
LABEL authors="ptttr"

WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=3000"]
