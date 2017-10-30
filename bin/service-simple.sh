#!/bin/bash

cd ~/src/stockstatspark/distribution/target/stockstat-distribution-0.5-SNAPSHOT-bin/stockstat-distribution-0.5-SNAPSHOT/lib

xterm -e "java -jar stockstat-eureka-0.5-SNAPSHOT.jar" &
xterm -e "java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18000,suspend=n -jar stockstat-core-0.5-SNAPSHOT.jar" &
xterm -e "java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18001,suspend=n -jar stockstat-iclij-core-0.5-SNAPSHOT.jar" &

cd ../tensorflow
xterm -e ./run-tf.sh &
xterm -e ./run-ke.sh &
