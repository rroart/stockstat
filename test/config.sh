    GEMCONFIG[1]="\"gemSConfig\" : { \"name\" : \"single\", ,\"steps\" : 100, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.1, \"data_file\" : \"\" }"
    GEMCONFIG[2]="\"gemIConfig\" : { \"name\" : \"independent\", \"steps\" : 100, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.1, \"finetune\" : false, \"cuda\" : false, \"data_file\" : \"\" }"
    GEMCONFIG[3]="\"gemMMConfig\" : { \"name\" : \"multimodal\", \"steps\" : 100, \"n_layers\" : 1, \"n_hiddens\" : 100, \"lr\" : 0.1, \"data_file\" : \"\" }"
    GEMCONFIG[4]="\"gemEWCConfig\" : { \"name\" : \"EWC\", \"steps\" : 100, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.1, \"n_memories\" : 10, \"memory_strength\" : 1, \"data_file\" : \"\" }"
    GEMCONFIG[5]="\"gemGEMConfig\" : { \"name\" : \"GEM\", \"steps\" : 100, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.1, \"n_memories\" : 256, \"memory_strength\" : 0.5, \"cuda\" : false, \"data_file\" : \"\" }"
    GEMCONFIG[6]="\"gemiCaRLConfig\" : { \"name\" : \"iCaRL\", \"steps\" : 100, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 1.0, \"n_memories\" : 1280, \"memory_strength\" : 1, \"samples_per_task\" : 10, \"data_file\" : \"\" }"
    FILENAME[1]=\"s\"
    FILENAME[2]=\"i\"
    FILENAME[3]=\"m\"
    FILENAME[4]=\"e\"
    FILENAME[5]=\"g\"
    FILENAME[6]=\"c\"

    PYTORCHCONFIG[1]="\"pytorchMLPConfig\" : { \"name\" : \"mlp\", \"steps\" : 1000, \"hiddenneurons\" : 20, \"hiddenlayers\":3, \"lr\" : 0.1 }"
    PYTORCHCONFIG[2]="\"pytorchRNNConfig\" : { \"name\" : \"rnn\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.1 }"
    PYTORCHCONFIG[3]="\"pytorchLSTMConfig\" : { \"name\" : \"lstm\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.1 }"
    PYTORCHCONFIG[4]="\"pytorchGRUConfig\" : { \"name\" : \"gru\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.1 }"
    PYTORCHCONFIG[5]="\"pytorchCNNConfig\" : { \"name\" : \"cnn\", \"steps\" : 1000, \"stride\" : 1, \"kernelsize\" : 4 }"
    
    TENSORFLOWCONFIG[1]="\"tensorflowDNNConfig\" : { \"name\" : \"dnn\", \"steps\" : 100, \"hiddenneurons\" : 20, \"hiddenlayers\":3 }"
    TENSORFLOWCONFIG[2]="\"tensorflowLICConfig\" : { \"name\" : \"lic\", \"steps\" : 100 }"
    TENSORFLOWCONFIG[3]="\"tensorflowMLPConfig\" : { \"name\" : \"mlp\", \"steps\" : 1000, \"hiddenneurons\" : 20, \"hiddenlayers\":3 }"
    TENSORFLOWCONFIG[4]="\"tensorflowRNNConfig\" : { \"name\" : \"rnn\", \"steps\" : 100, \"hiddenneurons\" : 100, \"hiddenlayers\" : 2, \"lr\" : 0.001 }"
    TENSORFLOWCONFIG[5]="\"tensorflowCNNConfig\" : { \"name\" : \"cnn\", \"steps\" : 100, \"stride\" : 1, \"kernelsize\" : 4 }"
    TENSORFLOWCONFIG[6]="\"tensorflowLSTMConfig\" : { \"name\" : \"lstm\", \"steps\" : 100, \"hiddenneurons\" : 100, \"hiddenlayers\" : 2, \"lr\" : 0.001 }"
    TENSORFLOWCONFIG[7]="\"tensorflowGRUConfig\" : { \"name\" : \"gru\", \"steps\" : 100, \"hiddenneurons\" : 100, \"hiddenlayers\" : 2, \"lr\" : 0.001 }"
    TENSORFLOWCONFIG[8]="\"tensorflowLIRConfig\" : { \"name\" : \"lir\", \"steps\" : 100 }"