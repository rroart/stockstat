#!/bin/bash

source irisdata.sh

OUTCOMES=3
SIZE=4

PORT=8008

if [ "$1" = "t" ]; then
    PORT=8008
    MODELS=3
    CONFIG[1]="\"tensorflowDNNConfig\": { \"name\":\"DNN\",\"steps\": 100, \"hiddenunits\" : [10,20,10], \"hiddenlayers\":3 }"
    CONFIG[2]="\"tensorflowLConfig\": { \"steps\": 100 }"
    CONFIG[3]="\"tensorflowMLPConfig\": { \"name\":\"DNN\",\"steps\": 100, \"hiddenunits\" : 20, \"hiddenlayers\":3 }"
fi

if [ "$1" = "p" ]; then
    PORT=8018
    MODELS=1
    CONFIG[1]="\"pytorchMLPConfig\": { \"name\":\"DNN\",\"steps\": 100, \"hiddenunits\" : 20, \"hiddenlayers\":3, \"lr\": 0.1 }"
    CONFIG[2]="\"pytorchRNNConfig\": { \"steps\": 1000, \"hidden\": 2, \"layers\": 2, \"lr\": 0.1 }"
fi

if [[ "$1" =~ ^g ]]; then
    PORT=8028
    MODELS=5
    CONFIG[1]="\"gemSConfig\": { \"steps\": 100, \"n_layers\": 2, \"n_hiddens\": 100, \"lr\": 0.1, \"data_file\": \"\" }"
    CONFIG[2]="\"gemIConfig\": { \"steps\": 100, \"n_layers\": 2, \"n_hiddens\": 100, \"lr\": 0.1, \"finetune\": false, \"cuda\": false, \"data_file\": \"\" }"
    CONFIG[3]="\"gemMConfig\": { \"steps\": 100, \"n_layers\": 1, \"n_hiddens\": 100, \"lr\": 0.1, \"data_file\": \"\" }"
    CONFIG[4]="\"gemEWCConfig\": { \"steps\": 100, \"n_layers\": 2, \"n_hiddens\": 100, \"lr\": 0.1, \"n_memories\": 10, \"memory_strength\": 1, \"data_file\": \"\" }"
    CONFIG[5]="\"gemGEMConfig\": { \"steps\": 100, \"n_layers\": 2, \"n_hiddens\": 100, \"lr\": 0.1, \"n_memories\": 256, \"memory_strength\": 0.5, \"cuda\": false, \"data_file\": \"\" }"
    CONFIG[6]="\"gemiCaRLConfig\": { \"steps\": 100, \"n_layers\": 2, \"n_hiddens\": 100, \"lr\": 1.0, \"n_memories\": 1280, \"memory_strength\": 1, \"samples_per_task\" : 10, \"data_file\": \"\" }"
    FILENAME[1]=\"s\"
    FILENAME[2]=\"i\"
    FILENAME[3]=\"m\"
    FILENAME[4]=\"e\"
    FILENAME[5]=\"g\"
    FILENAME[6]=\"c\"
fi

if [[ ! "$1" =~ ^g ]]; then
    for I in `seq 1 $MODELS`; do
	echo
	echo "Model" $I
	curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\": $I, \"classes\": $OUTCOMES, ${CONFIG[I]}, \"size\": $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\": $IRISTESTLABELS, \"zero\": true }" localhost:$PORT/learntest
	curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\": $I, \"classes\": $OUTCOMES, ${CONFIG[I]}, \"size\": $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\": $IRISTESTLABELS, \"classifyarray\": $IRISTEST, \"zero\": true }" localhost:$PORT/learntestclassify
    done
fi

if [ "$1" = "g" ]; then
    for I in `seq 1 $MODELS`; do
	echo
	echo "Model" $I
	curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\": $I, \"classes\": $OUTCOMES, ${CONFIG[I]}, \"size\": $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\": $IRISTESTLABELS, \"zero\": true, \"filename\": ${FILENAME[I]}, \"save\": false }" localhost:$PORT/learntest
	curl -i -d "{ \"trainingarray\" : $IRIS, \"trainingcatarray\" : ${IRISLABELS}, \"modelInt\": $I, \"classes\": $OUTCOMES, ${CONFIG[I]}, \"size\": $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\": $IRISTESTLABELS, \"classifyarray\" : $IRISTEST, \"zero\": true, \"filename\": ${FILENAME[I]}, \"save\": false }" localhost:$PORT/learntestclassify
	echo
	echo "Model life " $I 
	for J in `seq 1 $ARRLEN`; do
	    curl -i -d "{ \"trainingarray\" : ${IRISARR[J]}, \"trainingcatarray\" : ${IRISLABELSARR[J]}, \"modelInt\": $I, \"classes\": $OUTCOMES, ${CONFIG[I]}, \"size\": $SIZE, \"testarray\" : $IRISTEST, \"testcatarray\": $IRISTESTLABELS, \"zero\": true, \"filename\": ${FILENAME[I]} }" localhost:$PORT/learntest
	done
    done
fi

if [ "$1" = "g2" ]; then
    for I in `seq 1 $MODELS`; do
	echo
	echo "Model life " $I
	for J in `seq 1 $ARRLEN0`; do
	    curl -i -d "{ \"trainingarray\" : ${IRISARR0[J]}, \"trainingcatarray\" : ${IRISLABELSARR0[J]}, \"modelInt\": $I, \"classes\": $OUTCOMES, ${CONFIG[I]}, \"size\": $SIZE, \"testarray\" : ${IRISTESTARR0[J]}, \"testcatarray\": ${IRISTESTLABELSARR0[J]}, \"zero\": true, \"filename\": ${FILENAME[I]} }" localhost:$PORT/learntest
	done
    done
fi

if [ "$1" = "gt" ]; then
    for I in `seq 1 $MODELS`; do
	echo
	echo "Model life " $I
	curl -i -d "{ \"modelInt\": $I, \"classes\": $OUTCOMES, ${CONFIG[I]}, \"size\": $SIZE, \"testarray\" : ${IRISTEST}, \"testcatarray\": ${IRISTESTLABELS}, \"zero\": true, \"filename\": ${FILENAME[I]} }" localhost:$PORT/test
    done
fi

if [ "$1" = "gc" ]; then
    for I in `seq 1 $MODELS`; do
	echo
	echo "Model life " $I
	curl -i -d "{ \"modelInt\": $I, \"classes\": $OUTCOMES, ${CONFIG[I]}, \"size\": $SIZE, \"classifyarray\" : ${IRISTEST}, \"zero\": true, \"filename\": ${FILENAME[I]} }" localhost:$PORT/classify
    done
fi

