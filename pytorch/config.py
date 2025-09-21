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

PYTORCHCOMMON = { 'steps' : 1000, 'lr' : 0.001, 'inputdropout' : 0.5, 'dropout' : 0.5, 'normalize' : True, 'batchnormalize' : True, 'regularize' : True, 'batchsize' : 64 }
PYTORCHMLPCONFIG = { 'name' : 'mlp', 'hidden' : 100, 'layers': 1, **PYTORCHCOMMON }
PYTORCHRNNCONFIG = { 'name' : 'rnn', 'hidden' : 100, 'layers' : 2, **PYTORCHCOMMON }
PYTORCHLSTMCONFIG = { 'name' : 'lstm', 'hidden' : 100, 'layers' : 2, **PYTORCHCOMMON }
PYTORCHGRUCONFIG = { 'name' : 'gru', 'hidden' : 100, 'layers' : 2, **PYTORCHCOMMON }
PYTORCHCNNCONFIG = { 'name' : 'cnn', 'stride' : 1, 'kernelsize' : 4, **PYTORCHCOMMON }
PYTORCHCNN2CONFIG = { 'name' : 'cnn2', 'stride' : 1, 'kernelsize' : 3, 'maxpool': 4, 'dropout1': 0.25, 'dropout2' : 0.5, 'lr' : 0.01, **PYTORCHCOMMON }
PYTORCHGPTMIDICONFIG = { 'name' : 'gptmidi', 'steps' : 1 }
PYTORCHGPTMIDIRPRCONFIG = { 'name' : 'gptmidirpr', 'rpr' : True, 'steps' : 1 }
PYTORCHGPTMIDIFIGAROCONFIG = { 'name' : 'gptmidifigaro', 'steps' : 1 }
PYTORCHGPTMIDIMMTCONFIG = { 'name' : 'gptmidimmt', 'steps' : 50, 'valid_steps' : 10 }

def get(cf):
    if cf == PYTORCHMLP:
        return cf, 1, PYTORCHMLPCONFIG
    elif cf == PYTORCHRNN:
        return cf, 2, PYTORCHRNNCONFIG
    elif cf == PYTORCHLSTM:
        return cf, 3, PYTORCHLSTMCONFIG
    elif cf == PYTORCHGRU:
        return cf, 4, PYTORCHGRUCONFIG
    elif cf == PYTORCHCNN:
        return cf, 5, PYTORCHCNNCONFIG
    elif cf == PYTORCHCNN2:
        return cf, 6, PYTORCHCNN2CONFIG
    elif cf == PYTORCHGPTMIDI:
        return cf, 7, PYTORCHGPTMIDICONFIG
    elif cf == PYTORCHGPTMIDIRPR:
        return cf, 8, PYTORCHGPTMIDIRPRCONFIG
    elif cf == PYTORCHGPTMIDIFIGARO:
        return cf, 9, PYTORCHGPTMIDIFIGAROCONFIG
    elif cf == PYTORCHGPTMIDIMMT:
        return cf, 10, PYTORCHGPTMIDIMMTCONFIG
    return None
