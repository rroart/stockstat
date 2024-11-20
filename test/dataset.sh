#!/bin/bash

source config.sh

OUTCOMES=3
SIZE=4

HOST=afree
HOST=localhost
PORT=8008

if [ "$1" = "t" ]; then
    PORT=8008
    MODELS="`seq 1 7` 9"
    CONFIG=( "" "${TENSORFLOWCONFIG[@]}" )
    TIMEMODELS="3 4 6 7 8"
    TIMECONFIG=( "" "${TENSORFLOWCONFIG[@]}" )
fi

if [ "$1" = "p" ]; then
    PORT=8018
    MODELS=`seq 1 6`
    CONFIG=( "" "${PYTORCHCONFIG[@]}" )
    TIMEMODELS="`seq 1 4`"
    TIMECONFIG=( "" "${PYTORCHCONFIG[@]}" )
fi

if [[ "$1" =~ ^g ]]; then
    PORT=8048
    MODELS=`seq 1 5`
    CONFIG=( "" "${GEMCONFIG[@]}" )
fi

if [ -n "$2" ]; then
    MODELS=$2
fi

DATASETARR[1]=\"mnist\"
DATASETARR[2]=\"cifar10\"
DATASETARRLEN=${#DATASETARR[@]}
#DATASETARRLEN=0

DATASETS=${DATASETARR[@]}

OUTCOMESARR[1]=10
OUTCOMESARR[2]=10

TIMEDATASETARR[1]=\"dailymintemperatures\"
TIMEDATASETARR[2]=\"number\"
TIMEDATASETARR[3]=\"nasdaq\"
TIMEDATASETARRLEN=${#TIMEDATASETARR[@]}
#TIMEDATASETARRLEN=0

NNC="\"neuralnetcommand\":{\"mllearn\":true,\"mlclassify\":false,\"mldynamic\":false,\"mlcross\":false}"

mkdir -p /tmp/datasets

if [[ ! "$1" =~ ^g ]]; then
    for I in $MODELS; do
	for J in `seq 1 $DATASETARRLEN`; do
	    echo
	    echo "Model" $I
	    curl -i -d "{ \"trainingarray\" : [], \"trainingcatarray\" : [], \"dataset\" : ${DATASETARR[J]}, \"modelInt\" : $I, \"classes\" : ${OUTCOMESARR[J]}, ${CONFIG[I]}, \"size\" : $SIZE, \"zero\" : true, $NNC }" $HOST:$PORT/dataset
	done
    done
    for I in $TIMEMODELS; do
	for J in `seq 1 $TIMEDATASETARRLEN`; do
	    echo
	    echo "Model" $I
	    curl -i -d "{ \"trainingarray\" : [], \"trainingcatarray\" : [], \"dataset\" : ${TIMEDATASETARR[J]}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${TIMECONFIG[I]}, \"size\" : $SIZE, \"zero\" : true, $NNC }" $HOST:$PORT/dataset
	done
    done
fi

if [ "$1" = "g" ]; then
    for I in $MODELS; do
	for J in `seq 1 $DATASETARRLEN`; do
	    echo
	    echo "Model" $I
	    curl -i -d "{ \"trainingarray\" : [], \"trainingcatarray\" : [], \"dataset\" : ${DATASETARR[J]}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"zero\" : true, \"filename\" : ${FILENAME[I]}, \"save\" : false, $NNC }" $HOST:$PORT/dataset
	done
    done
fi

