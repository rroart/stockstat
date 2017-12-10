#!/bin/sh
DIRNAME=`dirname $0`
$DIRNAME/tradstoc.sh
JAVA_HOME=/opt/jdk8 java -jar $DIRNAME/../input/target/stockstat-input-0.5-SNAPSHOT-jar-with-dependencies.jar /tmp/tradstoc.xml
