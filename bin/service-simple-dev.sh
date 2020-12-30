#!/bin/bash

export MYPORT=22345
export MYIPORT=22346

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $DIR/java10.sh

DEV="--spring.profiles.active=dev"

DB="-Dconnection.url=jdbc:postgresql://localhost:5432/stockstatdev"

cd ../lib

#xterm -e "java -jar stockstat-eureka-0.6-SNAPSHOT.jar 2>&1 | tee /tmp/eurekadev.out" &
xterm -e "$JAVA10 $DB -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18000,suspend=n -jar stockstat-core-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/coredev.out" &
xterm -e "java $DB -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18001,suspend=n -jar stockstat-iclij-core-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/iclijdev.out " &
xterm -e "java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18002,suspend=n -jar stockstat-iclij-web-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/iclijwebdev.out" &
xterm -e "java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18003,suspend=n -jar stockstat-web-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/webdev.out" &

cd ../tensorflow
xterm -e "./flasktf.sh 8008 2>&1 | tee /tmp/flasktfdev.out" &

cd ../pytorch
xterm -e "./flaskpt.sh 8018 2>&1 | tee /tmp/flaskptdev.out" &

cd ../gem
xterm -e "./flaskgem.sh 8048 2>&1 | tee /tmp/flaskgemdev.out" &

cd ../../../..

cd ../webr
xterm -e "npm run dev" &

cd ../weba
xterm -e "npm start" &

cd ../iclij/iclij-webr
xterm -e "npm run dev" &

cd ../iclij-weba
xterm -e "npm start" &

#cd ../python/pd
#xterm -e ./flaskpd.sh &
