#!/bin/sh
DIRNAME=`dirname $0`
$DIRNAME/tradstoc.sh
java -jar $DIRNAME/../input/target/stockstat-input-0.6-SNAPSHOT-jar-with-dependencies.jar /tmp/tradstoc.xml
