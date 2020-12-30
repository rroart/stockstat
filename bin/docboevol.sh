#!/bin/sh
DIRNAME=`dirname $0`
$DIRNAME/cboevol.sh
java -jar $DIRNAME/../input/target/stockstat-input-0.6-SNAPSHOT-jar-with-dependencies.jar /tmp/cboevol.xml
