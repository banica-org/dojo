#!/bin/bash

git clone --depth 1 --no-tags https://$1:$2@github.com/stef2georg/test-child
cd test-child && mvn package && cd ..
java -cp test-parent-1.0-SNAPSHOT-jar-with-dependencies.jar:test-child/target/test-child-1.0-SNAPSHOT.jar test.parent.Main