#!/bin/sh
DIRNAME=`dirname $0`
$DIRNAME/cboevol.sh
JAVA_HOME=/opt/jdk8 java -jar $DIRNAME/../input/target/stockstat-input-0.4-HBASE.jar /tmp/cboevol.xml
