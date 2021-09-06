#!/bin/bash

web=0
ml=0
core=0
icore=0
isim=0
ievolve=0

if [ "$1" == "" ]; then
    web=1
    ml=1
    core=1
    icore=1
    isim=1
    ievolve=1
else
    while [ "$1" != "" ]; do
	[ "$1" == "w" ] &&  web=1
	[ "$1" == "m" ] &&  ml=1
	[ "$1" == "c" ] &&  core=1
	[ "$1" == "i" ] &&  icore=1
	[ "$1" == "s" ] &&  isim=1
	[ "$1" == "e" ] &&  ievolve=1
	shift 1
    done
fi

JFR="-Xlog:gc*=debug:file=/tmp/gc.log:utctime,uptime,tid,level:filecount=10,filesize=128m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump.hprof -XX:StartFlightRecording=disk=true,dumponexit=true,filename=/tmp/recording.jfr,maxsize=1024m,maxage=1d,settings=profile,path-to-gc-roots=true"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $DIR/java11.sh

cd ../lib

#java -jar stockstat-eureka-0.6-SNAPSHOT.jar 2>&1 | tee /tmp/eureka.out > /dev/null 2>&1 &
if [ $core -eq 1 ]; then
    $COMMAND "$JAVA11 $DB $COREDEBUG -jar stockstat-core-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/core$OUTNAME.out $REDIRECT" &
fi
if [ $icore -eq 1 ]; then
    $COMMAND "java $DB $ICOREDEBUG -jar stockstat-iclij-core-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/iclij$OUTNAME.out $REDIRECT" &
fi
if [ $web -eq 1 ]; then
    $COMMAND "java $DB $IWEBCOREDEBUG -jar stockstat-iclij-webcore-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/iclijwebcore$OUTNAME.out $REDIRECT" &
    $COMMAND "java -jar $IWEBDEBUG stockstat-iclij-web-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/iclijweb$OUTNAME.out $REDIRECT" &
    $COMMAND "java -jar $WEBDEBUG stockstat-web-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/web$OUTNAME.out $REDIRECT" &
fi
if [ $isim -eq 1 ]; then
    $COMMAND "java $DB -Dconfig=isim.xml $ISIMDEBUG -jar stockstat-iclij-sim-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/isim$OUTNAME.out $REDIRECT" &
fi
if [ $ievolve -eq 1 ]; then
    $COMMAND "java $DB -Dconfig=ievolve.xml $IEVOLVEDEBUG -jar stockstat-iclij-evolve-0.6-SNAPSHOT.jar $DEV 2>&1 | tee /tmp/ievolve$OUTNAME.out $REDIRECT" &
fi
if [ $ml -eq 1 ]; then
    cd ../tensorflow
    $COMMAND "./flasktf.sh $TENSORFLOWSERVERPORT 2>&1 | tee /tmp/flasktf$OUTNAME.out" &

    cd ../pytorch
    $COMMAND "./flaskpt.sh $PYTORCHSERVERPORT 2>&1 | tee /tmp/flaskpt$OUTNAME.out" &

    cd ../gem
    $COMMAND "./flaskgem.sh $GEMSERVERPORT 2>&1 | tee /tmp/flaskgem$OUTNAME.out" &
fi
if [ $web -eq 1 ]; then
    cd ../webr/docroot
    $COMMAND "http-server -p $WEBR" &

    cd ../../iclij-webr/docroot
    $COMMAND "http-server -p $IWEBR" &

    cd ../weba/dist
    $COMMAND "http-server -p $WEBA" &

    cd ../../iclij-weba/dist
    $COMMAND "http-server -p $IWEBA" &
fi

