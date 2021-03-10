# user_name

FROM maven:3.6.3-openjdk-15 AS parent-build
ARG user_name
ARG repo_name
WORKDIR /app
#RUN git clone --depth 1 --no-tags https://github.com/stef2georg/test-parent
RUN git clone --depth 1 --no-tags https://github.com/$user_name/$repo_name
#RUN cd test-parent && mvn package
RUN cd $repo_name && mvn package

FROM maven:3.6.3-openjdk-15
ARG user_name
ARG repo_name
WORKDIR /app
#COPY --from=parent-build /app/test-parent/target/test-parent-1.0-SNAPSHOT-jar-with-dependencies.jar /app/test-parent-1.0-SNAPSHOT-jar-with-dependencies.jar
COPY --from=parent-build /app/$repo_name/target/${repo_name}-1.0-SNAPSHOT-jar-with-dependencies.jar /app/${repo_name}-1.0-SNAPSHOT-jar-with-dependencies.jar
COPY --from=parent-build /root/.m2 /root/.m2
COPY run.sh run.sh
# $0 is username, $1 is password/token for Github, $2 is child username, $3 is child repo name, $4 is parent-repo-name
ENTRYPOINT chmod +x run.sh && ./run.sh $0 $1 $2 $3 $4