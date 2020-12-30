#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $DIR/java10.sh

cd ../lib

#java -jar stockstat-eureka-0.6-SNAPSHOT.jar 2>&1 | tee /tmp/eureka.out > /dev/null 2>&1 &
$COMMAND "$JAVA10 $DB $COREDEBUG -jar stockstat-core-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/core$OUTNAME.out $REDIRECT" &
$COMMAND "java $DB $ICOREDEBUG -jar stockstat-iclij-core-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/iclij$OUTNAME.out $REDIRECT" &
$COMMAND "java $DB $IWEBCOREDEBUG -jar stockstat-iclij-webcore-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/iclijwebcore$OUTNAME.out $REDIRECT" &
$COMMAND "java -jar $IWEBDEBUG stockstat-iclij-web-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/iclijweb$OUTNAME.out $REDIRECT" &
$COMMAND "java -jar $WEBDEBUG stockstat-web-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/web$OUTNAME.out $REDIRECT" &

cd ../tensorflow
$COMMAND "./flasktf.sh $TENSORFLOWSERVERPORT 2>&1 | tee /tmp/flasktf$OUTNAME.out" &

cd ../pytorch
$COMMAND "./flaskpt.sh $PYTORCHSERVERPORT 2>&1 | tee /tmp/flaskpt$OUTNAME.out" &

cd ../gem
$COMMAND "./flaskgem.sh $GEMSERVERPORT 2>&1 | tee /tmp/flaskgem$OUTNAME.out" &

cd ../webr/docroot
$COMMAND "http-server -p $WEBR" &

cd ../../iclij-webr/docroot
$COMMAND "http-server -p $IWEBR" &

cd ../weba/dist
$COMMAND "http-server -p $WEBA" &

cd ../../iclij-weba/dist
$COMMAND "http-server -p $IWEBA" &
