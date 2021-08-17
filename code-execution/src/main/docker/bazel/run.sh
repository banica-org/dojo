#!/bin/bash

echo -------------------------- parent project build ------------------------------------
cd parent/dojo-docker-parent && bazel build //src/test/java:tests
echo ------------------------- start the spring rest api --------------------------------
cd ../../dojo-container-event-executor/event-executor/target && \
java -jar event-executor-0.0.1-SNAPSHOT.jar