#!/bin/bash

source timedata.sh

source config.sh

if [ "$1" = "p" ]; then
    PORT=8018
    MODELS="1"
    TIMECONFIG[1]="\"pytorchMLPConfig\" : { \"name\" : \"mlp\", \"steps\" : 1000, \"hidden\" : 20, \"layers\":3, \"lr\" : 0.1 }"
    TIMECONFIG[2]="\"pytorchRNNConfig\" : { \"name\" : \"rnn\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"slide_stride\" : 2, \"lr\" : 0.01 }"
fi

if [ "$1" = "t" ]; then
    PORT=8008
    MODELS="3"
    TIMECONFIG[3]="\"tensorflowMLPConfig\" : { \"name\" : \"mlp\", \"steps\" : 1000, \"hidden\" : 20, \"layers\":3, \"lr\" : 0.1 }"
    TIMECONFIG[4]="\"tensorflowRNNConfig\" : { \"name\" : \"rnn\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"slide_stride\" : 2, \"lr\" : 0.01 }"
fi

if [[ ! "$1" =~ ^g ]]; then
    for I in $MODELS; do
	echo
	echo "Model" $I
	SIZE=5
	OUTCOMES=4
	for J in `seq 1 $TIMESLIDEARRLEN`; do
	    curl -i -d "{ \"trainingarray\" : ${TIMESLIDEARR[J]}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${TIMECONFIG[I]}, \"size\" : $SIZE, \"testarray\" : $TIMETESTSLIDE, \"testcatarray\" : $TIMETESTLABELSSLIDE, \"zero\" : true }" localhost:$PORT/learntest
	    exit
	done
	SIZE=2
	OUTCOMES=3
	for J in `seq 1 $TIMEARRLEN`; do
	    curl -i -d "{ \"trainingarray\" : ${TIMEARR[J]}, \"trainingcatarray\" : ${TIMELABELS[J]}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${TIMECONFIG[I]}, \"size\" : $SIZE, \"testarray\" : ${TIMETESTARR[J]}, \"testcatarray\" : ${TIMETESTLABELSARR[J]}, \"zero\" : true }" localhost:$PORT/learntest
	done
	for J in `seq 1 $TIMEARRLEN`; do
	    curl -i -d "{ \"trainingarray\" : ${TIMEARR[J]}, \"trainingcatarray\" : ${TIMELABELS[J]}, \"modelInt\" : $I, \"classes\" : $OUTCOMES, ${TIMECONFIG[I]}, \"size\" : $SIZE, \"testarray\" : ${TIMETESTARR[J]}, \"testcatarray\" : ${TIMETESTLABELSARR[J]}, \"classifyarray\" : ${TIMETESTARR[J]}, \"zero\" : true }" localhost:$PORT/learntestclassify
	done
    done
fi



