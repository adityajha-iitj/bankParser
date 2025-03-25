FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY target/pdf-parser-api-0.0.1-SNAPSHOT.jar app.jar

RUN chmod +x app.jar

EXPOSE 8080

CMD [ "java", "-jar", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75", "app.jar" ]