#!/bin/sh
cd /home/roart/src/stockstat/bin
./cboevol.sh
JAVA_HOME=/opt/jdk8 java -jar /home/roart/src/stockstat/input/target/stockstat-input-0.2-SNAPSHOT-jar-with-dependencies.jar /tmp/cboevol.xml
