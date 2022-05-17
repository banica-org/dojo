FROM openjdk:11
ADD target/notifications-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8090
CMD ["java", "-Dcodenjoy.host=dojoserver", "-Dkeycloak.host=keycloak", "-DCODE_EXECUTION_HOST=code-execution", "-jar", "app.jar"]