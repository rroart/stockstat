PYTORCHMLP = 'pytorchMLPConfig'
PYTORCHRNN = 'pytorchRNNConfig'
PYTORCHLSTM = 'pytorchLSTMConfig'
PYTORCHGRU = 'pytorchGRUConfig'
PYTORCHCNN = 'pytorchCNNConfig'
PYTORCHCNN2 = 'pytorchCNN2Config'
PYTORCHGPTMIDI = 'pytorchGPTMIDIConfig'
PYTORCHGPTMIDIRPR = 'pytorchGPTMIDIRPRConfig'
PYTORCHGPTMIDIFIGARO = 'pytorchGPTMIDIFigaroConfig'
PYTORCHGPTMIDIMMT = 'pytorchGPTMIDIMMTConfig'

TENSORFLOWDNN = 'tensorflowDNNConfig'
TENSORFLOWLIC = 'tensorflowLICConfig'
TENSORFLOWMLP = 'tensorflowMLPConfig'
TENSORFLOWRNN = 'tensorflowRNNConfig'
TENSORFLOWCNN = 'tensorflowCNNConfig'
TENSORFLOWCNN2 = 'tensorflowCNN2Config'
TENSORFLOWLSTM = 'tensorflowLSTMConfig'
TENSORFLOWGRU = 'tensorflowGRUConfig'
TENSORFLOWLIR = 'tensorflowLIRConfig'
TENSORFLOWQNN = 'tensorflowQNNConfig'
TENSORFLOWQCNN = 'tensorflowQCNNConfig'
TENSORFLOWDCGAN = 'tensorflowDCGANConfig'
TENSORFLOWCONDITIONALGAN = 'tensorflowConditionalGANConfig'
TENSORFLOWNEURALSTYLETRANSFER = 'tensorflowNeuralStyleTransferConfig'
TENSORFLOWMINIATUREGPT = 'tensorflowMiniatureGPTConfig'
TENSORFLOWGPT = 'tensorflowGPTConfig'
TENSORFLOWGPT2 = 'tensorflowGPT2Config'
TENSORFLOWPQK = 'tensorflowPQKConfig'
TENSORFLOWVAE = 'tensorflowVAEConfig'

PYTORCHMLPCONFIG = { 'name' : 'mlp', 'steps' : 1000, 'lr' : 0.001, 'dropout' : 0.5, 'normalize' : True, 'batchnormalize' : True, 'regularize' : True, 'hidden' : 100, 'layers': 2 }
PYTORCHRNNCONFIG = { 'name' : 'rnn', 'steps' : 1000, 'lr' : 0.001, 'dropout' : 0.5, 'normalize' : True, 'batchnormalize' : True, 'regularize' : True, 'hidden' : 100, 'layers' : 2 }
PYTORCHLSTMCONFIG = { 'name' : 'lstm', 'steps' : 1000, 'lr' : 0.001, 'dropout' : 0.5, 'normalize' : True, 'batchnormalize' : True, 'regularize' : True, 'hidden' : 100, 'layers' : 2, }
PYTORCHGRUCONFIG = { 'name' : 'gru', 'steps' : 1000, 'lr' : 0.001, 'dropout' : 0.5, 'normalize' : True, 'batchnormalize' : True, 'regularize' : True, 'hidden' : 100, 'layers' : 2 }
PYTORCHCNNCONFIG = { 'name' : 'cnn', 'steps' : 1000, 'lr' : 0.001, 'dropout' : 0.5, 'normalize' : True, 'batchnormalize' : True, 'regularize' : True, 'stride' : 1, 'kernelsize' : 4 }
PYTORCHCNN2CONFIG = { 'name' : 'cnn2', 'steps' : 1000, 'lr' : 0.001, 'dropout' : 0.5, 'normalize' : True, 'batchnormalize' : True, 'regularize' : True, 'stride' : 1, 'kernelsize' : 3, 'maxpool': 4 }
PYTORCHGPTMIDICONFIG = { 'name' : 'gptmidi', 'steps' : 1 }
PYTORCHGPTMIDIRPRCONFIG = { 'name' : 'gptmidirpr', 'rpr' : True, 'steps' : 1 }
PYTORCHGPTMIDIFIGAROCONFIG = { 'name' : 'gptmidifigaro', 'steps' : 1 }
PYTORCHGPTMIDIMMTCONFIG = { 'name' : 'gptmidimmt', 'steps' : 50, 'valid_steps' : 10 }

TENSORFLOWCOMMONCLASSIFY = { 'loss' : 'sparse_categorical_crossentropy', 'optimizer' : 'adam', 'activation' : 'relu', 'lastactivation' : 'softmax' }
TENSORFLOWCOMMONCLASSIFYCNN = { 'loss' : 'sparse_categorical_crossentropy', 'optimizer' : 'adam', 'activation' : 'leaky_relu', 'lastactivation' : 'softmax' }
TENSORFLOWCOMMONCLASSIFYCNN2 = { 'loss' : 'sparse_categorical_crossentropy', 'optimizer' : 'adadelta', 'activation' : 'leaky_relu', 'lastactivation' : 'softmax' }
TENSORFLOWCOMMONPREDICT = { 'loss' : 'mse', 'optimizer' : 'rmsprop', 'activation' : 'relu', 'lastactivation' : 'linear', 'inputdropout' : 0, 'dropout' : 0, 'normalize' : False, 'batchnormalize' : False, 'regularize' : False }
TENSORFLOWCOMMONPREDICTCNN = { 'loss' : 'mse', 'optimizer' : 'rmsprop', 'activation' : 'leakyrelu', 'lastactivation' : 'linear' }
TENSORFLOWCOMMON = { 'steps' : 1000, 'lr' : None, 'inputdropout' : 0.5, 'dropout' : 0.5, 'normalize' : True, 'batchnormalize' : True, 'regularize' : True }
TENSORFLOWDNNCONFIG = { 'name' : 'dnn', 'hidden' : 100, 'layers': 2, **TENSORFLOWCOMMON }
TENSORFLOWLICCONFIG = { 'name' : 'lic', **TENSORFLOWCOMMON }
TENSORFLOWMLPCONFIG = { 'name' : 'mlp', 'hidden' : 100, 'layers' : 2, **TENSORFLOWCOMMON }
TENSORFLOWRNNCONFIG = { 'name' : 'rnn', 'hidden' : 100, 'layers' : 2, **TENSORFLOWCOMMON }
TENSORFLOWCNNCONFIG = { 'name' : 'cnn', 'stride' : 1, 'kernelsize' : 4, **TENSORFLOWCOMMON }
TENSORFLOWCNN2CONFIG = { 'name' : 'cnn2', 'stride' : 1, 'kernelsize' : 3, 'maxpool': 4, **TENSORFLOWCOMMON }
TENSORFLOWLSTMCONFIG = { 'name' : 'lstm', 'hidden' : 15, 'layers' : 1, **TENSORFLOWCOMMON }
TENSORFLOWGRUCONFIG = { 'name' : 'gru', 'hidden' : 100, 'layers' : 2, **TENSORFLOWCOMMON }
TENSORFLOWLIRCONFIG = { 'name' : 'lir',  }
TENSORFLOWQNNCONFIG = { 'name' : 'qnn', 'steps' : 3 }
TENSORFLOWQCNNCONFIG = { 'name' : 'qcnn', 'steps' : 3 }
TENSORFLOWDCGANCONFIG = { 'name' : 'dcgan', 'steps' : 20, 'lr' : 0.0001 }
TENSORFLOWCONDITIONALGANCONFIG = { 'name' : 'conditionalgan', 'steps' : 20, 'lr' : 0.0003 }
TENSORFLOWNEURALSTYLETRANSFERCONFIG = { 'name' : 'neural_style_transfer', 'steps' : 20 }
TENSORFLOWMINIATUREGPTCONFIG = { 'name' : 'miniature_gpt', 'steps' : 25 }
TENSORFLOWGPTCONFIG = { 'name' : 'gpt', 'steps' : 5 }
TENSORFLOWGPT2CONFIG = { 'name' : 'gpt2', 'steps' : 1 }
TENSORFLOWPQKCONFIG = { 'name' : 'pqk', 'steps' : 3 }
TENSORFLOWVAECONFIG = { 'name' : 'vae', 'steps' : 30 }

PYTORCHCOMMONCLASSIFY = { 'loss' : 'cross_entropy', 'optimizer' : 'sgd', 'activation' : 'relu', 'lastactivation' : 'relu' }
PYTORCHCOMMONCLASSIFYCNN = { 'loss' : 'cross_entropy', 'optimizer' : 'sgd', 'activation' : 'relu', 'lastactivation' : 'relu' }
PYTORCHCOMMONCLASSIFYCNN2 = { 'loss' : 'cross_entropy', 'optimizer' : 'adadelta', 'activation' : 'relu', 'lastactivation' : 'relu' }
PYTORCHCOMMONPREDICT = { 'loss' : 'mse', 'optimizer' : 'rmsprop', 'activation' : 'relu', 'lastactivation' : 'linear' }
PYTORCHCOMMONPREDICTCNN = { 'loss' : 'mse', 'optimizer' : 'rmsprop', 'activation' : 'relu', 'lastactivation' : 'linear' }
PYTORCHCOMMON = { 'steps' : 1000, 'lr' : None, 'inputdropout' : 0.5, 'dropout' : 0.5, 'normalize' : True, 'batchnormalize' : True, 'regularize' : True, 'batchsize' : 64 }
PYTORCHMLPCONFIG = { 'name' : 'mlp', 'hidden' : 100, 'layers': 2, **PYTORCHCOMMON }
PYTORCHRNNCONFIG = { 'name' : 'rnn', 'hidden' : 100, 'layers' : 2, **PYTORCHCOMMON }
PYTORCHLSTMCONFIG = { 'name' : 'lstm', 'hidden' : 100, 'layers' : 2, **PYTORCHCOMMON }
PYTORCHGRUCONFIG = { 'name' : 'gru', 'hidden' : 100, 'layers' : 2, **PYTORCHCOMMON }
PYTORCHCNNCONFIG = { 'name' : 'cnn', 'stride' : 1, 'kernelsize' : 4, **PYTORCHCOMMON }
PYTORCHCNN2CONFIG = { 'name' : 'cnn2', 'stride' : 1, 'kernelsize' : 3, 'maxpool': 4, **PYTORCHCOMMON }
PYTORCHGPTMIDICONFIG = { 'name' : 'gptmidi', 'steps' : 1 }
PYTORCHGPTMIDIRPRCONFIG = { 'name' : 'gptmidirpr', 'rpr' : True, 'steps' : 1 }
PYTORCHGPTMIDIFIGAROCONFIG = { 'name' : 'gptmidifigaro', 'steps' : 1 }
PYTORCHGPTMIDIMMTCONFIG = { 'name' : 'gptmidimmt', 'steps' : 50, 'valid_steps' : 10 }

def get(cf, predictor = False):
    if cf == TENSORFLOWDNN:
        return cf, 1, TENSORFLOWDNNCONFIG
    elif cf == TENSORFLOWLIC:
        return cf, 2, TENSORFLOWLICCONFIG
    elif cf == TENSORFLOWMLP:
        if predictor:
            TENSORFLOWMLPCONFIG.update(TENSORFLOWCOMMONPREDICT)
        else:
            TENSORFLOWMLPCONFIG.update(TENSORFLOWCOMMONCLASSIFY)
        return cf, 3, TENSORFLOWMLPCONFIG
    elif cf == TENSORFLOWRNN:
        TENSORFLOWRNNCONFIG.update(TENSORFLOWCOMMONCLASSIFY)
        return cf, 4, TENSORFLOWRNNCONFIG
    elif cf == TENSORFLOWCNN:
        TENSORFLOWCNNCONFIG.update(TENSORFLOWCOMMONCLASSIFYCNN)
        return cf, 5, TENSORFLOWCNNCONFIG
    elif cf == TENSORFLOWLSTM:
        TENSORFLOWLSTMCONFIG.update(TENSORFLOWCOMMONCLASSIFY)
        return cf, 6, TENSORFLOWLSTMCONFIG
    elif cf == TENSORFLOWGRU:
        TENSORFLOWGRUCONFIG.update(TENSORFLOWCOMMONCLASSIFY)
        return cf, 7, TENSORFLOWGRUCONFIG
    elif cf == TENSORFLOWLIR:
        return cf, 8, TENSORFLOWLIRCONFIG
    elif cf == TENSORFLOWCNN2:
        TENSORFLOWCNN2CONFIG.update(TENSORFLOWCOMMONCLASSIFYCNN2)
        return cf, 9, TENSORFLOWCNN2CONFIG
    elif cf == TENSORFLOWQNN:
        return cf, 10, TENSORFLOWQNNCONFIG
    elif cf == TENSORFLOWQCNN:
        return cf, 11, TENSORFLOWQCNNCONFIG
    elif cf == TENSORFLOWCONDITIONALGAN:
        return cf, 12, TENSORFLOWCONDITIONALGANCONFIG
    elif cf == TENSORFLOWDCGAN:
        return cf, 13, TENSORFLOWDCGANCONFIG
    elif cf == TENSORFLOWNEURALSTYLETRANSFER:
        return cf, 14, TENSORFLOWNEURALSTYLETRANSFERCONFIG
    elif cf == TENSORFLOWMINIATUREGPT:
        return cf, 15, TENSORFLOWMINIATUREGPTCONFIG
    elif cf == TENSORFLOWGPT:
        return cf, 16, TENSORFLOWGPTCONFIG
    elif cf == TENSORFLOWGPT2:
        return cf, 17, TENSORFLOWGPT2CONFIG
    elif cf == TENSORFLOWPQK:
        return cf, 18, TENSORFLOWPQKCONFIG
    elif cf == TENSORFLOWVAE:
        return cf, 19, TENSORFLOWVAECONFIG
    if cf == PYTORCHMLP:
        if predictor:
            PYTORCHMLPCONFIG.update(PYTORCHCOMMONPREDICT)
        else:
            PYTORCHMLPCONFIG.update(PYTORCHCOMMONCLASSIFY)
        return cf, 1, PYTORCHMLPCONFIG
    elif cf == PYTORCHRNN:
        if predictor:
            PYTORCHRNNCONFIG.update(PYTORCHCOMMONPREDICT)
        else:
            PYTORCHRNNCONFIG.update(PYTORCHCOMMONCLASSIFY)
        return cf, 2, PYTORCHRNNCONFIG
    elif cf == PYTORCHLSTM:
        if predictor:
            PYTORCHLSTMCONFIG.update(PYTORCHCOMMONPREDICT)
        else:
            PYTORCHLSTMCONFIG.update(PYTORCHCOMMONCLASSIFY)
        return cf, 3, PYTORCHLSTMCONFIG
    elif cf == PYTORCHGRU:
        if predictor:
            PYTORCHGRUCONFIG.update(PYTORCHCOMMONPREDICT)
        else:
            PYTORCHGRUCONFIG.update(PYTORCHCOMMONCLASSIFY)
        return cf, 4, PYTORCHGRUCONFIG
    elif cf == PYTORCHCNN:
        PYTORCHCNNCONFIG.update(PYTORCHCOMMONCLASSIFYCNN)
        return cf, 5, PYTORCHCNNCONFIG
    elif cf == PYTORCHCNN2:
        PYTORCHCNN2CONFIG.update(PYTORCHCOMMONCLASSIFYCNN2)
        return cf, 6, PYTORCHCNN2CONFIG
    elif cf == PYTORCHGPTMIDI:
        return cf, 7, PYTORCHGPTMIDICONFIG
    elif cf == PYTORCHGPTMIDIRPR:
        return cf, 8, PYTORCHGPTMIDIRPRCONFIG
    elif cf == PYTORCHGPTMIDIFIGARO:
        return cf, 9, PYTORCHGPTMIDIFIGAROCONFIG
    elif cf == PYTORCHGPTMIDIMMT:
        return cf, 10, PYTORCHGPTMIDIMMTCONFIG
    print("Unknown config", cf)
    return None
