#!/bin/bash

WEBR=8182
IWEBR=8183
export MYPORT=12345
export MYIPORT=12346

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $DIR/java10.sh

cd ../lib

#java -jar stockstat-eureka-0.6-SNAPSHOT.jar 2>&1 | tee /tmp/eureka.out > /dev/null 2>&1 &
$JAVA10 -jar stockstat-core-0.6-SNAPSHOT.jar 2>&1 | tee /tmp/core.out > /dev/null 2>&1 &
java -jar stockstat-iclij-core-0.6-SNAPSHOT.jar 2>&1 | tee /tmp/iclij.out > /dev/null 2>&1 &
java -jar stockstat-iclij-web-0.6-SNAPSHOT.jar 2>&1 | tee /tmp/iclijweb.out > /dev/null 2>&1 &
java -jar stockstat-web-0.6-SNAPSHOT.jar 2>&1 | tee /tmp/web.out > /dev/null 2>&1 &

cd ../tensorflow
./flasktf.sh 8000 2>&1 | tee /tmp/flasktf.out &

cd ../pytorch
./flaskpt.sh 8010 2>&1 | tee /tmp/flaskpt.out &

cd ../gem
./flaskgem.sh 8040 2>&1 | tee /tmp/flaskgem.out &

cd ../webr/docroot
http-server -p $WEBR &

cd ../../iclij-webr/docroot
http-server -p $IWEBR &
