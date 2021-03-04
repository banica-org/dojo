FROM maven:3.6.3-openjdk-15 AS parent-build
WORKDIR /app
RUN git clone --depth 1 --no-tags https://github.com/stef2georg/test-parent
RUN cd test-parent && mvn package

FROM maven:3.6.3-openjdk-15
WORKDIR /app
COPY --from=parent-build /app/test-parent/target/test-parent-1.0-SNAPSHOT-jar-with-dependencies.jar /app/test-parent-1.0-SNAPSHOT-jar-with-dependencies.jar
COPY --from=parent-build /root/.m2 /root/.m2
COPY run.sh run.sh
# $0 is username and $1 is password/token for Github
ENTRYPOINT chmod +x run.sh && ./run.sh $0 $1