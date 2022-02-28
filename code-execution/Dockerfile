FROM java:openjdk-8-jre-alpine

WORKDIR app
COPY ./target/code-execution-1.0.0-SNAPSHOT.jar .
COPY ./src/main/docker/ app/docker

ENV DOCKER_FILEPATH="app/docker/Dockerfile"
ENV CODENJOY_ADDRESS="host.docker.internal"
ENV DOCKER_HOST="host.docker.internal"
ENV KEYCLOAK_HOST="host.docker.internal"

EXPOSE 9091
EXPOSE 8081
EXPOSE 2375

CMD java -jar code-execution-1.0.0-SNAPSHOT.jar -Ddocker.file=${DOCKER_FILEPATH} -Dcodenjoy.host=${CODENJOY_ADDRESS} -Ddocker.host=${DOCKER_HOST} -Dkeycloak.host=${KEYCLOAK_HOST}