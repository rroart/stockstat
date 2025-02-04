STEPS=1000
STEPS=1

    GEMCONFIG[1]="\"gemSConfig\" : { \"name\" : \"single\" ,\"steps\" : $STEPS, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.001, \"data_file\" : \"\" }"
    GEMCONFIG[2]="\"gemIConfig\" : { \"name\" : \"independent\", \"steps\" : $STEPS, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.001, \"finetune\" : false, \"cuda\" : false, \"data_file\" : \"\" }"
    GEMCONFIG[3]="\"gemMMConfig\" : { \"name\" : \"multimodal\", \"steps\" : $STEPS, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.001, \"data_file\" : \"\" }"
    GEMCONFIG[4]="\"gemEWCConfig\" : { \"name\" : \"EWC\", \"steps\" : $STEPS, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.001, \"n_memories\" : 10, \"memory_strength\" : 1, \"data_file\" : \"\" }"
    GEMCONFIG[5]="\"gemGEMConfig\" : { \"name\" : \"GEM\", \"steps\" : $STEPS, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.001, \"n_memories\" : 256, \"memory_strength\" : 0.5, \"cuda\" : false, \"data_file\" : \"\" }"
    GEMCONFIG[6]="\"gemIcarlConfig\" : { \"name\" : \"iCaRL\", \"steps\" : $STEPS, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.001, \"n_memories\" : 1280, \"memory_strength\" : 1, \"samples_per_task\" : 10, \"cuda\" : false, \"data_file\" : \"\" }"
    FILENAME[1]=\"s\"
    FILENAME[2]=\"i\"
    FILENAME[3]=\"m\"
    FILENAME[4]=\"e\"
    FILENAME[5]=\"g\"
    FILENAME[6]=\"c\"

    PYTORCHCONFIG[1]="\"pytorchMLPConfig\" : { \"name\" : \"mlp\", \"steps\" : $STEPS, \"hidden\" : 100, \"layers\": 2, \"lr\" : 0.01 }"
    PYTORCHCONFIG[2]="\"pytorchRNNConfig\" : { \"name\" : \"rnn\", \"steps\" : $STEPS, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01 }"
    PYTORCHCONFIG[3]="\"pytorchLSTMConfig\" : { \"name\" : \"lstm\", \"steps\" : $STEPS, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01 }"
    PYTORCHCONFIG[4]="\"pytorchGRUConfig\" : { \"name\" : \"gru\", \"steps\" : $STEPS, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01 }"
    PYTORCHCONFIG[5]="\"pytorchCNNConfig\" : { \"name\" : \"cnn\", \"steps\" : $STEPS, \"stride\" : 1, \"kernelsize\" : 4, \"dropout\" : 0.5, \"lr\" : 0.01 }"
    PYTORCHCONFIG[6]="\"pytorchCNN2Config\" : { \"name\" : \"cnn2\", \"steps\" : $STEPS, \"stride\" : 1, \"kernelsize\" : 3, \"maxpool\" : 4, \"dropout1\" : 0.25, \"dropout2\" : 0.5, \"lr\" : 0.01 }"
    
    TENSORFLOWCONFIG[1]="\"tensorflowDNNConfig\" : { \"name\" : \"dnn\", \"steps\" : $STEPS, \"hidden\" : 100, \"layers\": 2, \"lr\" : 0.01 }"
    TENSORFLOWCONFIG[2]="\"tensorflowLICConfig\" : { \"name\" : \"lic\", \"steps\" : $STEPS, \"lr\" : 0.01 }"
    TENSORFLOWCONFIG[3]="\"tensorflowMLPConfig\" : { \"name\" : \"mlp\", \"steps\" : $STEPS, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01 }"
    TENSORFLOWCONFIG[4]="\"tensorflowRNNConfig\" : { \"name\" : \"rnn\", \"steps\" : $STEPS, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01, \"dropout\" : 0, \"dropoutin\" : 0 }"
    TENSORFLOWCONFIG[5]="\"tensorflowCNNConfig\" : { \"name\" : \"cnn\", \"steps\" : $STEPS, \"stride\" : 1, \"kernelsize\" : 4, \"dropout\" : 0.5 }"
    TENSORFLOWCONFIG[6]="\"tensorflowLSTMConfig\" : { \"name\" : \"lstm\", \"steps\" : $STEPS, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01, \"dropout\" : 0, \"dropoutin\" : 0 }"
    TENSORFLOWCONFIG[7]="\"tensorflowGRUConfig\" : { \"name\" : \"gru\", \"steps\" : $STEPS, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01, \"dropout\" : 0, \"dropoutin\" : 0 }"
    TENSORFLOWCONFIG[8]="\"tensorflowLIRConfig\" : { \"name\" : \"lir\", \"steps\" : $STEPS, \"lr\" : 0.01 }"
    TENSORFLOWCONFIG[9]="\"tensorflowCNN2Config\" : { \"name\" : \"cnn2\", \"steps\" : $STEPS, \"stride\" : 1, \"kernelsize\" : 3, \"maxpool\" : 4, \"dropout1\" : 0.25, \"dropout2\" : 0.5, \"lr\" : 0.01 }"
