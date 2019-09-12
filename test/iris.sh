#!/bin/bash

source irisdata.sh

source config.sh

OUTCOMES=3
SIZE=4

PORT=8008

if [ "$1" = "t" ]; then
    PORT=8008
    MODELS='8'
    CONFIG=( "" "${TENSORFLOWCONFIG[@]}" )
fi

if [ "$1" = "p" ]; then
    PORT=8018
    MODELS=1
    CONFIG=( "" "${PYTORCHCONFIG[@]}" )
fi

if [[ "$1" =~ ^g ]]; then
    PORT=8028
    MODELS=5
    CONFIG=( "" "${GEMCONFIG[@]}" )
fi

if [[ ! "$1" =~ ^g ]]; then
    for I in $MODELS; do
	echo
	echo "Model" $I
	curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\" : $IRISTESTLABELS, \"zero\" : true }" localhost:$PORT/learntest
	curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\" : $IRISTESTLABELS, \"classifyarray\" : $IRISTEST, \"zero\" : true }" localhost:$PORT/learntestclassify
    done
fi

if [ "$1" = "g" ]; then
    for I in $MODELS; do
	echo
	echo "Model" $I
	curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\" : $IRISTESTLABELS, \"zero\" : true, \"filename\" : ${FILENAME[I]}, \"save\" : false }" localhost:$PORT/learntest
	curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\" : $IRISTESTLABELS, \"classifyarray\" : $IRISTEST, \"zero\" : true, \"filename\" : ${FILENAME[I]}, \"save\" : false }" localhost:$PORT/learntestclassify
	echo
	echo "Model life " $I 
	for J in `seq 1 $ARRLEN`; do
	    curl -i -d "{ \"trainingarray\" : ${IRISARR[J]}, \"trainingcatarray\" : ${IRISLABELSARR[J]}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\" : $IRISTESTLABELS, \"zero\" : true, \"filename\" : ${FILENAME[I]} }" localhost:$PORT/learntest
	done
    done
fi

if [ "$1" = "g2" ]; then
    for I in $MODELS; do
	echo
	echo "Model life " $I
	for J in `seq 1 $ARRLEN0`; do
	    curl -i -d "{ \"trainingarray\" : ${IRISARR0[J]}, \"trainingcatarray\" : ${IRISLABELSARR0[J]}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : ${IRISTESTARR0[J]}, \"testcatarray\" : ${IRISTESTLABELSARR0[J]}, \"zero\" : true, \"filename\" : ${FILENAME[I]} }" localhost:$PORT/learntest
	done
    done
fi

if [ "$1" = "gt" ]; then
    for I in $MODELS; do
	echo
	echo "Model life " $I
	curl -i -d "{ \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"testarray\" : ${IRISTEST}, \"testcatarray\" : ${IRISTESTLABELS}, \"zero\" : true, \"filename\" : ${FILENAME[I]} }" localhost:$PORT/test
    done
fi

if [ "$1" = "gc" ]; then
    for I in $MODELS; do
	echo
	echo "Model life " $I
	curl -i -d "{ \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"classifyarray\" : ${IRISTEST}, \"zero\" : true, \"filename\" : ${FILENAME[I]} }" localhost:$PORT/classify
    done
fi

