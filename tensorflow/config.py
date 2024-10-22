TENSORFLOWDNNCONFIG = { 'name' : 'dnn', 'steps' : 1000, 'hidden' : 100, 'layers': 2, 'lr' : 0.01 }
TENSORFLOWLICCONFIG = { 'name' : 'lic', 'steps' : 1000, 'lr' : 0.01 }
TENSORFLOWMLPCONFIG = { 'name' : 'mlp', 'steps' : 1000, 'hidden' : 100, 'layers' : 2, 'lr' : 0.01 }
TENSORFLOWRNNCONFIG = { 'name' : 'rnn', 'steps' : 1000, 'hidden' : 100, 'layers' : 2, 'lr' : 0.01, 'dropout' : 0, 'dropoutin' : 0 }
TENSORFLOWCNNCONFIG = { 'name' : 'cnn', 'steps' : 1000, 'stride' : 1, 'kernelsize' : 4, 'dropout' : 0.5 }
TENSORFLOWCNN2CONFIG = { 'name' : 'cnn2', 'steps' : 1000, 'stride' : 1, 'kernelsize' : 3, 'maxpool': 4, 'dropout1': 0.25, 'dropout2' : 0.5 }
TENSORFLOWLSTMCONFIG = { 'name' : 'lstm', 'steps' : 1000, 'hidden' : 100, 'layers' : 2, 'lr' : 0.01, 'dropout' : 0, 'dropoutin' : 0 }
TENSORFLOWGRUCONFIG = { 'name' : 'gru', 'steps' : 1000, 'hidden' : 100, 'layers' : 2, 'lr' : 0.01, 'dropout' : 0, 'dropoutin' : 0 }
TENSORFLOWLIRCONFIG = { 'name' : 'lir', 'steps' : 1000, 'lr' : 0.01 }
TENSORFLOWDCGANCONFIG = { 'name' : 'dcgan', 'steps' : 20, 'lr' : 0.0001 }
TENSORFLOWCONDITIONALGANCONFIG = { 'name' : 'conditionalgan', 'steps' : 20, 'lr' : 0.0003 }
TENSORFLOWNEURALSTYLETRANSFERCONFIG = { 'name' : 'neural_style_transfer', 'steps' : 20 }
TENSORFLOWMINIATUREGPTCONFIG = { 'name' : 'miniature_gpt', 'steps' : 25 }
TENSORFLOWGPTCONFIG = { 'name' : 'gpt', 'steps' : 5 }
TENSORFLOWGPT2CONFIG = { 'name' : 'gpt2', 'steps' : 1 }

def get(cf):
    if cf == 'tensorflowDNNConfig':
        return cf, 1, TENSORFLOWDNNCONFIG
    elif cf == 'tensorflowLICConfig':
        return cf, 2, TENSORFLOWLICCONFIG
    elif cf == 'tensorflowMLPConfig':
        return cf, 3, TENSORFLOWMLPCONFIG
    elif cf == 'tensorflowRNNConfig':
        return cf, 4, TENSORFLOWRNNCONFIG
    elif cf == 'tensorflowCNNConfig':
        return cf, 5, TENSORFLOWCNNCONFIG
    elif cf == 'tensorflowLSTMConfig':
        return cf, 6, TENSORFLOWLSTMCONFIG
    elif cf == 'tensorflowGRUConfig':
        return cf, 7, TENSORFLOWGRUCONFIG
    elif cf == 'tensorflowLIRConfig':
        return cf, 8, TENSORFLOWLIRCONFIG
    elif cf == 'tensorflowCNN2Config':
        return cf, 9, TENSORFLOWCNN2CONFIG
    elif cf == 'tensorflowConditionalGANConfig':
        return cf, 12, TENSORFLOWCONDITIONALGANCONFIG
    elif cf == 'tensorflowDCGANConfig':
        return cf, 13, TENSORFLOWDCGANCONFIG
    elif cf == 'tensorflowNeuralStyleTransferConfig':
        return cf, 14, TENSORFLOWNEURALSTYLETRANSFERCONFIG
    elif cf == 'tensorflowMiniatureGPTConfig':
        return cf, 15, TENSORFLOWMINIATUREGPTCONFIG
    elif cf == 'tensorflowGPTConfig':
        return cf, 16, TENSORFLOWGPTCONFIG
    elif cf == 'tensorflowGPT2Config':
        return cf, 17, TENSORFLOWGPT2CONFIG
    return None
