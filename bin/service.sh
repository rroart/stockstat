#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $DIR/java10.sh

cd ../lib

java -jar stockstat-eureka-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/eureka.out > /dev/null 2>&1 &
$JAVA10 -jar stockstat-core-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/core.out > /dev/null 2>&1 &
java -jar stockstat-iclij-core-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/iclij.out > /dev/null 2>&1 &
java -jar stockstat-iclij-web-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/iclijweb.out > /dev/null 2>&1 &
java -jar stockstat-web-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/web.out > /dev/null 2>&1 &

cd ../tensorflow
./flasktf.sh 2>&1 | tee /tmp/flask.out &
