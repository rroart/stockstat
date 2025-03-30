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

PYTORCHMLPCONFIG = { 'name' : 'mlp', 'steps' : 1000, 'hidden' : 100, 'layers': 2, 'lr' : 0.01 }
PYTORCHRNNCONFIG = { 'name' : 'rnn', 'steps' : 1000, 'hidden' : 100, 'layers' : 2, 'lr' : 0.01 }
PYTORCHLSTMCONFIG = { 'name' : 'lstm', 'steps' : 1000, 'hidden' : 100, 'layers' : 2, 'lr' : 0.01 }
PYTORCHGRUCONFIG = { 'name' : 'gru', 'steps' : 1000, 'hidden' : 100, 'layers' : 2, 'lr' : 0.01 }
PYTORCHCNNCONFIG = { 'name' : 'cnn', 'steps' : 1000, 'stride' : 1, 'kernelsize' : 4, 'dropout' : 0.5, 'lr' : 0.01 }
PYTORCHCNN2CONFIG = { 'name' : 'cnn2', 'steps' : 1000, 'stride' : 1, 'kernelsize' : 3, 'maxpool': 4, 'dropout1': 0.25, 'dropout2' : 0.5, 'lr' : 0.01 }
PYTORCHGPTMIDICONFIG = { 'name' : 'gptmidi', 'steps' : 1 }
PYTORCHGPTMIDIRPRCONFIG = { 'name' : 'gptmidirpr', 'rpr' : True, 'steps' : 1 }
PYTORCHGPTMIDIFIGAROCONFIG = { 'name' : 'gptmidifigaro', 'steps' : 1 }
PYTORCHGPTMIDIMMTCONFIG = { 'name' : 'gptmidimmt', 'steps' : 1 }

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
