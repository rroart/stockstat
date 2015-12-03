#!/bin/sh
cd /home/roart/src/bors
./bors.sh
JAVA_HOME=/opt/jdk8 java -jar input/target/stockstat-input-0.1-SNAPSHOT-jar-with-dependencies.jar bors.xml
