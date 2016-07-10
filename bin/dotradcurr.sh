#!/bin/sh
cd /home/roart/src/stockstat/bin
./tradcurr.sh
JAVA_HOME=/opt/jdk8 java -jar /home/roart/src/stockstat/input/target/stockstat-input-0.3-SNAPSHOT-jar-with-dependencies.jar /tmp/tradcurr.xml
