FROM java:openjdk-8-jre-alpine

WORKDIR app
COPY ./target/notifications-1.0.0-SNAPSHOT.jar .
ENV CODENJOY_HOST="host.docker.internal"

EXPOSE 8090
EXPOSE 9092

CMD java -jar notifications-1.0.0-SNAPSHOT.jar -Dcodenjoy.host=${CODENJOY_HOST}