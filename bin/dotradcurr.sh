#!/bin/sh
DIRNAME=`dirname $0`
$DIRNAME/tradcurr.sh
JAVA_HOME=/opt/jdk8 java -jar $DIRNAME/../input/target/stockstat-input-0.5-SNAPSHOT-jar-with-dependencies.jar /tmp/tradcurr.xml
