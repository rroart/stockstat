#!/bin/bash

source config.sh

OUTCOMES=3
SIZE=4

PORT=8008

if [ "$1" = "t" ]; then
    PORT=8008
    MODELS=`seq 1 7`
    CONFIG=( "" "${TENSORFLOWCONFIG[@]}" )
fi

if [ "$1" = "p" ]; then
    PORT=8018
    MODELS=`seq 1 5`
    CONFIG=( "" "${PYTORCHCONFIG[@]}" )
    TIMEMODELS=2
    TIMECONFIG[2]="\"pytorchRNNConfig\" : { \"name\" : \"rnn\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"slide_stride\" : 2, \"lr\" : 0.01 }"
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
DATASETARRLEN=${#DATASETARR[@]}

OUTCOMESARR[1]=10

TIMEDATASETARR[1]=\"dailymintemperatures\"
TIMEDATASETARRLEN=${#TIMEDATASETARR[@]}

mkdir -p /tmp/datasets

if [[ ! "$1" =~ ^g ]]; then
    for I in $MODELS; do
	for J in `seq 1 $DATASETARRLEN`; do
	    echo
	    echo "Model" $I
	    curl -i -d "{ \"dataset\" : ${DATASETARR[J]}, \"modelInt\" : $I, \"classes\" : ${OUTCOMESARR[J]}, ${CONFIG[I]}, \"size\" : $SIZE, \"zero\" : true }" localhost:$PORT/dataset
	done
    done
    for I in $TIMEMODELS; do
	for J in `seq 1 $TIMEDATASETARRLEN`; do
	    echo
	    echo "Model" $I
	    curl -i -d "{ \"dataset\" : ${TIMEDATASETARR[J]}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${TIMECONFIG[I]}, \"size\" : $SIZE, \"zero\" : true }" localhost:$PORT/dataset
	done
    done
fi

if [ "$1" = "g" ]; then
    for I in $MODELS; do
	for J in `seq 1 $DATASETARRLEN`; do
	    echo
	    echo "Model" $I
	    curl -i -d "{ \"dataset\" : ${DATASETARR[J]}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${CONFIG[I]}, \"size\" : $SIZE, \"zero\" : true, \"filename\" : ${FILENAME[I]}, \"save\" : false }" localhost:$PORT/dataset
	done
    done
fi

