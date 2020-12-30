#!/bin/sh
DIRNAME=`dirname $0`
$DIRNAME/tradcurr.sh
java -jar $DIRNAME/../input/target/stockstat-input-0.6-SNAPSHOT-jar-with-dependencies.jar /tmp/tradcurr.xml
