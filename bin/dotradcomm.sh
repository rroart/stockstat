#!/bin/sh
DIRNAME=`dirname $0`
$DIRNAME/tradcomm.sh
java -jar $DIRNAME/../input/target/stockstat-input-0.5-SNAPSHOT-jar-with-dependencies.jar /tmp/tradcomm.xml
