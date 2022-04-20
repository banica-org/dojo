FROM maven:3.6.3-openjdk-15 AS parent-build
ARG user_name
ARG repo_name
WORKDIR /app
#RUN git clone --depth 1 --no-tags https://githubAccessToken@github.com/stef2georg/test-parent
RUN git clone --depth 1 --no-tags https://githubAccessToken@github.com/$user_name/$repo_name
#RUN cd test-parent && mvn package
RUN cd $repo_name && mvn -DskipTests package

#FROM gradle:7.0.0-jdk16
#ARG user_name
#ARG repo_name
#WORKDIR /app
#COPY --from=parent-build /app/$repo_name/target/${repo_name}-1.0-SNAPSHOT-solution-tests.jar /app/${repo_name}-1.0-SNAPSHOT-solution-tests.jar
#COPY run.sh run.sh

FROM maven:3.6.3-openjdk-15
ARG user_name
ARG repo_name
ARG client_id_var
ARG client_secret_var
ENV client_id $client_id_var
ENV client_secret $client_secret_var
WORKDIR /app
#COPY --from=parent-build /app/$repo_name/target/${repo_name}-1.0-SNAPSHOT-jar-with-dependencies.jar /app/${repo_name}-1.0-SNAPSHOT-jar-with-dependencies.jar
COPY --from=parent-build /app/$repo_name/target/${repo_name}-1.0-SNAPSHOT-solution-tests.jar /app/${repo_name}-1.0-SNAPSHOT-solution-tests.jar
#COPY --from=parent-build /root/.m2 /root/.m2
COPY --from=parent-build /root/.m2 /usr/share/maven/ref
COPY run.sh run.sh
# $0 is username, $1 is password/token for Github, $2 is child username, $3 is child repo name, $4 is parent-repo-name
ENTRYPOINT chmod +x run.sh && ./run.sh $0 $1 $2 $3 $4 $5