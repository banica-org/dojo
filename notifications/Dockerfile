FROM java:openjdk-8-jre-alpine

WORKDIR app
COPY ./target/notifications-1.0.0-SNAPSHOT.jar .
ENV CODENJOY_HOST="172.20.0.1"

EXPOSE 8090

CMD java -jar notifications-1.0.0-SNAPSHOT.jar -Dcodenjoy.host=${CODENJOY_HOST}