#!/bin/bash

cd ../lib

xterm -e "java -jar stockstat-eureka-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/eureka.out" &
xterm -e "java -jar stockstat-core-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/core.out" &
xterm -e "java -jar stockstat-iclij-core-0.5-SNAPSHOT.jar 2>&1 | tee /tmp/iclij.out" &
xterm -e "java -jar jetty-runner-9.4.12.v20180830.jar --port 8181 stockstat-iclij-web-0.5-SNAPSHOT.war 2>&1 | tee /tmp/iclijweb.out" &
xterm -e "java -jar jetty-runner-9.4.12.v20180830.jar --port 8180 stockstat-web-0.5-SNAPSHOT.war 2>&1 | tee /tmp/web.out" &

cd ../tensorflow
xterm -e "./flasktf.sh 2>&1 | tee /tmp/flask.out" &

#cd ../python/pd
#xterm -e ./flaskpd.sh &
