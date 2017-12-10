#!/bin/bash

cd ../lib

xterm -e "java -jar stockstat-eureka-0.5-SNAPSHOT.jar" &
xterm -e "java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18000,suspend=n -jar stockstat-core-0.5-SNAPSHOT.jar" &
xterm -e "java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18001,suspend=n -jar stockstat-iclij-core-0.5-SNAPSHOT.jar" &
xterm -e "java -jar jetty-runner-9.4.2.v20170220.jar --port 8082 stockstat-iclij-web-0.5-SNAPSHOT.war" &
xterm -e "java -jar jetty-runner-9.4.2.v20170220.jar stockstat-web-0.5-SNAPSHOT.war" &

cd ../tensorflow
xterm -e ./run-tf.sh &
xterm -e ./run-ke.sh &
