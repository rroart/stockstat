#!/bin/bash

source timedata.sh

if [ "$1" = "p" ]; then
    PORT=8018
    MODELS="2"
    CONFIG[1]="\"pytorchMLPConfig\": { \"name\":\"MLP\",\"steps\": 100, \"hiddenunits\" : 20, \"hiddenlayers\":3, \"lr\": 0.1 }"
    CONFIG[2]="\"pytorchRNNConfig\": { \"steps\": 1000, \"hidden\": 2, \"layers\": 2, \"slide_stride\": 2, \"lr\": 0.1 }"
fi

if [[ ! "$1" =~ ^g ]]; then
    for I in $MODELS; do
	echo
	echo "Model" $I
	SIZE=5
	OUTCOMES=4
	for J in `seq 1 $TIMESLIDEARRLEN`; do
	     curl -i -d "{ \"trainingarray\" : ${TIMESLIDEARR[J]}, \"modelInt\": $I, \"classes\": $OUTCOMES, ${CONFIG[2]}, \"size\": $SIZE, \"testarray\" : $TIMETESTSLIDE, \"testcatarray\": $TIMETESTLABELSSLIDE, \"zero\": true }" localhost:$PORT/learntest
	done
	SIZE=2
	OUTCOMES=3
	for J in `seq 1 $TIMEARRLEN`; do
	    curl -i -d "{ \"trainingarray\" : ${TIMEARR[J]}, \"trainingcatarray\" : ${TIMELABELS[J]}, \"modelInt\": $I, \"classes\": $OUTCOMES, ${CONFIG[2]}, \"size\": $SIZE, \"testarray\" : ${TIMETESTARR[J]}, \"testcatarray\": ${TIMETESTLABELSARR[J]}, \"zero\": true }" localhost:$PORT/learntest
	done
	for J in `seq 1 $TIMEARRLEN`; do
	    curl -i -d "{ \"trainingarray\" : ${TIMEARR[J]}, \"trainingcatarray\" : ${TIMELABELS[J]}, \"modelInt\": $I, \"classes\": $OUTCOMES, ${CONFIG[2]}, \"size\": $SIZE, \"testarray\" : ${TIMETESTARR[J]}, \"testcatarray\": ${TIMETESTLABELSARR[J]}, \"classifyarray\" : ${TIMETESTARR[J]}, \"zero\": true }" localhost:$PORT/learntestclassify
	done
    done
fi



