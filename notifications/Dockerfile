FROM java:openjdk-8-jre-alpine

RUN mkdir App
WORKDIR App
COPY ./target/notifications-1.0.0-SNAPSHOT.jar .

EXPOSE 8090

CMD java -jar notifications-1.0.0-SNAPSHOT.jar