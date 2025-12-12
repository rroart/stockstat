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

PYTORCHCOMMONCLASSIFY = { 'loss' : 'cross_entropy', 'optimizer' : 'sgd', 'activation' : 'relu', 'lastactivation' : 'softmax' }
PYTORCHCOMMONCLASSIFYCNN = { 'loss' : 'cross_entropy', 'optimizer' : 'sgd', 'activation' : 'relu', 'lastactivation' : 'softmax' }
PYTORCHCOMMONCLASSIFYCNN2 = { 'loss' : 'cross_entropy', 'optimizer' : 'adadelta', 'activation' : 'relu', 'lastactivation' : 'softmax' }
PYTORCHCOMMONPREDICT = { 'loss' : 'mse', 'optimizer' : 'rmsprop', 'activation' : 'relu', 'lastactivation' : 'linear' }
PYTORCHCOMMONPREDICTCNN = { 'loss' : 'mse', 'optimizer' : 'rmsprop', 'activation' : 'relu', 'lastactivation' : 'linear' }
PYTORCHCOMMON = { 'steps' : 1000, 'lr' : None, 'inputdropout' : 0.25, 'dropout' : 0.2, 'normalize' : True, 'batchnormalize' : True, 'regularize' : True, 'batchsize' : 64 }
PYTORCHCOMMONFEED = { 'hidden' : 100, 'layers' : 2 }
PYTORCHCOMMONPREFEED = {'hidden' : 100, 'layers': 2, 'convlayers' : 3 }
PYTORCHMLPCONFIG = { 'name' : 'mlp', **PYTORCHCOMMONFEED, **PYTORCHCOMMON }
PYTORCHRNNCONFIG = { 'name' : 'rnn', **PYTORCHCOMMONFEED, **PYTORCHCOMMON }
PYTORCHLSTMCONFIG = { 'name' : 'lstm', **PYTORCHCOMMONFEED, **PYTORCHCOMMON }
PYTORCHGRUCONFIG = { 'name' : 'gru', **PYTORCHCOMMONFEED, **PYTORCHCOMMON }
PYTORCHCNNCONFIG = { 'name' : 'cnn', 'stride' : 1, 'kernelsize' : 4, 'maxpool': 2, **PYTORCHCOMMONPREFEED, **PYTORCHCOMMON }
PYTORCHCNN2CONFIG = { 'name' : 'cnn2', 'stride' : 1, 'kernelsize' : 3, 'maxpool': 2, **PYTORCHCOMMONPREFEED, **PYTORCHCOMMON }
PYTORCHGPTMIDICONFIG = { 'name' : 'gptmidi', 'steps' : 1 }
PYTORCHGPTMIDIRPRCONFIG = { 'name' : 'gptmidirpr', 'rpr' : True, 'steps' : 1 }
PYTORCHGPTMIDIFIGAROCONFIG = { 'name' : 'gptmidifigaro', 'steps' : 1 }
PYTORCHGPTMIDIMMTCONFIG = { 'name' : 'gptmidimmt', 'steps' : 50, 'valid_steps' : 10 }

def get(cf, predictor = False, binary = False):
    if cf == PYTORCHMLP:
        if predictor:
            PYTORCHMLPCONFIG.update(PYTORCHCOMMONPREDICT)
        else:
            PYTORCHMLPCONFIG.update(PYTORCHCOMMONCLASSIFY)
        if binary and not predictor:
            PYTORCHMLPCONFIG['loss'] = 'bce'
            PYTORCHMLPCONFIG['lastactivation'] = 'sigmoid'
        return cf, 1, PYTORCHMLPCONFIG
    elif cf == PYTORCHRNN:
        if predictor:
            PYTORCHRNNCONFIG.update(PYTORCHCOMMONPREDICT)
        else:
            PYTORCHRNNCONFIG.update(PYTORCHCOMMONCLASSIFY)
        if binary and not predictor:
            PYTORCHRNNCONFIG['loss'] = 'bce'
            PYTORCHRNNCONFIG['lastactivation'] = 'sigmoid'
        return cf, 2, PYTORCHRNNCONFIG
    elif cf == PYTORCHLSTM:
        if predictor:
            PYTORCHLSTMCONFIG.update(PYTORCHCOMMONPREDICT)
        else:
            PYTORCHLSTMCONFIG.update(PYTORCHCOMMONCLASSIFY)
        if binary and not predictor:
            PYTORCHLSTMCONFIG['loss'] = 'bce'
            PYTORCHLSTMCONFIG['lastactivation'] = 'sigmoid'
        return cf, 3, PYTORCHLSTMCONFIG
    elif cf == PYTORCHGRU:
        if predictor:
            PYTORCHGRUCONFIG.update(PYTORCHCOMMONPREDICT)
        else:
            PYTORCHGRUCONFIG.update(PYTORCHCOMMONCLASSIFY)
        if binary and not predictor:
            PYTORCHGRUCONFIG['loss'] = 'bce'
            PYTORCHGRUCONFIG['lastactivation'] = 'sigmoid'
        return cf, 4, PYTORCHGRUCONFIG
    elif cf == PYTORCHCNN:
        PYTORCHCNNCONFIG.update(PYTORCHCOMMONCLASSIFYCNN)
        if binary and not predictor:
            PYTORCHCNNCONFIG['loss'] = 'bce'
            PYTORCHCNNCONFIG['lastactivation'] = 'sigmoid'
        return cf, 5, PYTORCHCNNCONFIG
    elif cf == PYTORCHCNN2:
        PYTORCHCNN2CONFIG.update(PYTORCHCOMMONCLASSIFYCNN2)
        if binary and not predictor:
            PYTORCHCNN2CONFIG['loss'] = 'bce'
            PYTORCHCNN2CONFIG['lastactivation'] = 'sigmoid'
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
