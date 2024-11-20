#!/bin/bash

source irisdata.sh

source config.sh

OUTCOMES=3
SIZE=4

HOST=afree
HOST=localhost
PORT=8008

if [ "$1" = "t" ]; then
    PORT=8008
    MODELS=`seq 1 3`
    CONFIG=( "" "${TENSORFLOWCONFIG[@]}" )
fi

if [ "$1" = "p" ]; then
    PORT=8018
    MODELS=`seq 1 1`
    CONFIG=( "" "${PYTORCHCONFIG[@]}" )
fi

if [[ "$1" =~ ^g ]]; then
    PORT=8048
    MODELS=`seq 1 5`
    CONFIG=( "" "${GEMCONFIG[@]}" )
fi

if [ -n "$2" ]; then
    MODELS=$2
fi

if [[ ! "$1" =~ ^g ]]; then
    for I in $MODELS; do
	echo
	echo "Model" $I
	if [ "$3" = "s" ]; then
	    EXTRA1=", \"filename\" : \"$I\", \"neuralnetcommand\" : { \"mldynamic\" : false, \"mllearn\" : true, \"mlclassify\" : false }"
	    EXTRA2=", \"filename\" : \"$I\", \"neuralnetcommand\" : { \"mldynamic\" : false, \"mllearn\" : false, \"mlclassify\" : true }"
	    curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\" : $IRISTESTLABELS, \"zero\" : true $EXTRA1 }" $HOST:$PORT/learntestclassify
	    curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\" : $IRISTESTLABELS, \"classifyarray\" : $IRISTEST, \"zero\" : true $EXTRA2 }" $HOST:$PORT/learntestclassify
	else
	    curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\" : $IRISTESTLABELS, \"zero\" : true }" $HOST:$PORT/learntest
	    curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\" : $IRISTESTLABELS, \"classifyarray\" : $IRISTEST, \"zero\" : true }" $HOST:$PORT/learntestclassify
	fi
    done
fi

if [ "$1" = "g" ]; then
    for I in $MODELS; do
	echo
	echo "Model" $I
	curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\" : $IRISTESTLABELS, \"zero\" : true, \"filename\" : ${FILENAME[I]}, \"save\" : false }" $HOST:$PORT/learntest
	curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\" : $IRISTESTLABELS, \"classifyarray\" : $IRISTEST, \"zero\" : true, \"filename\" : ${FILENAME[I]}, \"save\" : false }" $HOST:$PORT/learntestclassify
	echo
	echo "Model life " $I 
	for J in `seq 1 $ARRLEN`; do
	    curl -i -d "{ \"trainingarray\" : ${IRISARR[J]}, \"trainingcatarray\" : ${IRISLABELSARR[J]}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\" : $IRISTESTLABELS, \"zero\" : true, \"filename\" : ${FILENAME[I]} }" $HOST:$PORT/learntest
	done
    done
fi

if [ "$1" = "g2" ]; then
    for I in $MODELS; do
	echo
	echo "Model life " $I
	for J in `seq 1 $ARRLEN0`; do
	    curl -i -d "{ \"trainingarray\" : ${IRISARR0[J]}, \"trainingcatarray\" : ${IRISLABELSARR0[J]}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : ${IRISTESTARR0[J]}, \"testcatarray\" : ${IRISTESTLABELSARR0[J]}, \"zero\" : true, \"filename\" : ${FILENAME[I]} }" $HOST:$PORT/learntest
	done
    done
fi

if [ "$1" = "gt" ]; then
    for I in $MODELS; do
	echo
	echo "Model life " $I
	curl -i -d "{ \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : ${IRISTEST}, \"testcatarray\" : ${IRISTESTLABELS}, \"zero\" : true, \"filename\" : ${FILENAME[I]} }" $HOST:$PORT/test
    done
fi

if [ "$1" = "gc" ]; then
    for I in $MODELS; do
	echo
	echo "Model life " $I
	curl -i -d "{ \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"classifyarray\" : ${IRISTEST}, \"zero\" : true, \"filename\" : ${FILENAME[I]} }" $HOST:$PORT/classify
    done
fi

