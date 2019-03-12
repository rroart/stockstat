#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $DIR/java10.sh

DEV="--spring.profiles.active=dev"

DB="-Dconnection.url=jdbc:postgresql://localhost:5432/stockstatdev"

cd ../lib

xterm -e "java -jar stockstat-eureka-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/eurekadev.out" &
xterm -e "$JAVA10 $DB -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18000,suspend=n -jar stockstat-core-0.5-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/coredev.out" &
xterm -e "java $DB -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18001,suspend=n -jar stockstat-iclij-core-0.5-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/iclijdev.out " &
xterm -e "java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18002,suspend=n -jar stockstat-iclij-web-0.5-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/iclijwebdev.out" &
xterm -e "java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18003,suspend=n -jar stockstat-web-0.5-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/webdev.out" &

cd ../tensorflow
xterm -e "./flasktf.sh dev 2>&1 | tee /tmp/flaskdev.out" &

#cd ../python/pd
#xterm -e ./flaskpd.sh &
