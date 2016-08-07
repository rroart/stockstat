#!/bin/sh
DIRNAME=`dirname $0`
$DIRNAME/tradcomm.sh
JAVA_HOME=/opt/jdk8 java -jar $DIRNAME/../input/target/stockstat-input-0.4-SNAPSHOT-jar-with-dependencies.jar /tmp/tradcomm.xml
