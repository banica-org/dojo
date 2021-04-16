#!/bin/bash

#git clone --depth 1 --no-tags https://$1:$2@github.com/stef2georg/test-child
git clone --depth 1 --no-tags https://$1:$2@github.com/$3/$4
#cd test-child && mvn package && cd ..
cd $4 && echo build-log-separator && mvn -DskipTests package && echo build-log-separator && cd .. && java -cp "${4}"/target/"${4}"-1.0-SNAPSHOT.jar:"${5}"-1.0-SNAPSHOT-solution-tests.jar TestMain $3
#java -cp test-parent-1.0-SNAPSHOT-jar-with-dependencies.jar:test-child/target/test-child-1.0-SNAPSHOT.jar test.parent.Main
echo build-log-separator