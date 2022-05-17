FROM openjdk:11
ADD target/code-execution-1.0.0-SNAPSHOT.jar app.jar
COPY src/main/resources/docker /docker
EXPOSE 8081
CMD ["java", "-Ddocker.host=host.docker.internal", "-Dcodenjoy.host=dojoserver", "-Dkeycloak.host=keycloak", "-Ddocker.file=/docker/Dockerfile", "-Dkeycloak.port=8080", "-jar", "app.jar"]