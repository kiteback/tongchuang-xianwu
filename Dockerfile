FROM eclipse-temurin:17-jre-ubi9-minimal

WORKDIR /app

COPY target/tongchuang-xianwu-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
