#!/bin/bash

#git clone --depth 1 --no-tags https://$1:$2@github.com/stef2georg/test-child
git clone --depth 1 --no-tags https://$1:$2@github.com/$3/$4
#cd test-child && mvn package && cd ..
cd $4 && mvn package && cd ..
#java -cp test-parent-1.0-SNAPSHOT-jar-with-dependencies.jar:test-child/target/test-child-1.0-SNAPSHOT.jar test.parent.Main
java -cp "${5}"-1.0-SNAPSHOT-jar-with-dependencies.jar:"${4}"/target/"${4}"-1.0-SNAPSHOT.jar test.parent.Main