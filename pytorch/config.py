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
