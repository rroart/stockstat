#!/bin/sh
cd /home/roart/src/stockstat/bin
./mornind.sh
JAVA_HOME=/opt/jdk8 java -jar /home/roart/src/stockstat/input/target/stockstat-input-0.1-SNAPSHOT-jar-with-dependencies.jar /tmp/mornind.xml
