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

TENSORFLOWDNNCONFIG = { 'name' : 'dnn', 'steps' : 1000, 'hidden' : 100, 'layers': 2, 'lr' : 0.01 }
TENSORFLOWLICCONFIG = { 'name' : 'lic', 'steps' : 1000, 'lr' : 0.01 }
TENSORFLOWMLPCONFIG = { 'name' : 'mlp', 'steps' : 1000, 'hidden' : 100, 'layers' : 2, 'lr' : 0.01 }
TENSORFLOWRNNCONFIG = { 'name' : 'rnn', 'steps' : 1000, 'hidden' : 100, 'layers' : 2, 'lr' : 0.01, 'dropout' : 0, 'dropoutin' : 0 }
TENSORFLOWCNNCONFIG = { 'name' : 'cnn', 'steps' : 1000, 'stride' : 1, 'kernelsize' : 4, 'dropout' : 0.5 }
TENSORFLOWCNN2CONFIG = { 'name' : 'cnn2', 'steps' : 1000, 'stride' : 1, 'kernelsize' : 3, 'maxpool': 4, 'dropout1': 0.25, 'dropout2' : 0.5 }
TENSORFLOWLSTMCONFIG = { 'name' : 'lstm', 'steps' : 1000, 'hidden' : 100, 'layers' : 2, 'lr' : 0.01, 'dropout' : 0, 'dropoutin' : 0 }
TENSORFLOWGRUCONFIG = { 'name' : 'gru', 'steps' : 1000, 'hidden' : 100, 'layers' : 2, 'lr' : 0.01, 'dropout' : 0, 'dropoutin' : 0 }
TENSORFLOWLIRCONFIG = { 'name' : 'lir', 'steps' : 1000, 'lr' : 0.01 }
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

def get(cf):
    if cf == TENSORFLOWDNN:
        return cf, 1, TENSORFLOWDNNCONFIG
    elif cf == TENSORFLOWLIC:
        return cf, 2, TENSORFLOWLICCONFIG
    elif cf == TENSORFLOWMLP:
        return cf, 3, TENSORFLOWMLPCONFIG
    elif cf == TENSORFLOWRNN:
        return cf, 4, TENSORFLOWRNNCONFIG
    elif cf == TENSORFLOWCNN:
        return cf, 5, TENSORFLOWCNNCONFIG
    elif cf == TENSORFLOWLSTM:
        return cf, 6, TENSORFLOWLSTMCONFIG
    elif cf == TENSORFLOWGRU:
        return cf, 7, TENSORFLOWGRUCONFIG
    elif cf == TENSORFLOWLIR:
        return cf, 8, TENSORFLOWLIRCONFIG
    elif cf == TENSORFLOWCNN2:
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
    return None
