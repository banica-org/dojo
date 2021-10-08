
FROM alpine/git:v2.30.2 AS parent-build
ARG user_name
ARG repo_name
WORKDIR /app
#RUN git clone --depth 1 --no-tags https://github.com/$user_name/$repo_name
RUN git clone --depth 1 --no-tags https://Dzhemile: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 @github.com/Dzhemile/dojo-docker-parent
RUN git clone --depth 1 --no-tags https://Dzhemile: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 @github.com/Dzhemile/dojo-container-event-executor

#FROM openjdk:11 AS bazel-build
FROM maven:3.6.3-openjdk-11 AS bazel-build
RUN echo "deb [arch=amd64] http://storage.googleapis.com/bazel-apt stable jdk1.8" | tee /etc/apt/sources.list.d/bazel.list \
 && curl https://bazel.build/bazel-release.pub.gpg | apt-key add -
RUN apt update && apt install -y bazel && rm -rf /var/lib/apt/lists/*
WORKDIR /app
RUN bazel --version

FROM bazel-build
ARG user_name
ARG repo_name
WORKDIR /app
RUN java -version
RUN mkdir build
COPY --from=parent-build /app/dojo-docker-parent /app/parent/dojo-docker-parent
COPY --from=parent-build /app/dojo-container-event-executor /app/dojo-container-event-executor
COPY run.sh run.sh
RUN cd parent/dojo-docker-parent && bazel fetch //...
RUN cd dojo-container-event-executor/event-executor && mvn -DskipTests clean package spring-boot:repackage
EXPOSE 8085
ENTRYPOINT chmod +x run.sh && ./run.sh
