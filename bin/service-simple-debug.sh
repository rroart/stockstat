#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $DIR/java10.sh

cd ../lib

xterm -e "java -jar stockstat-eureka-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/eureka.out" &
xterm -e "$JAVA10 -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18000,suspend=n -jar stockstat-core-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/core.out" &
xterm -e "java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18001,suspend=n -jar stockstat-iclij-core-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/iclij.out " &
xterm -e "java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18002,suspend=n -jar stockstat-iclij-web-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/iclijweb.out" &
xterm -e "java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18003,suspend=n -jar stockstat-web-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/web.out" &

cd ../tensorflow
xterm -e "./flasktf.sh 2>&1 | tee /tmp/flasktf.out" &

cd ../pytorch
xterm -e "./flaskpt.sh 2>&1 | tee /tmp/flaskpt.out" &

cd ../gem
xterm -e "./flaskgem.sh 2>&1 | tee /tmp/flaskgem.out" &

#cd ../python/pd
#xterm -e ./flaskpd.sh &
