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

TENSORFLOWCOMMONCLASSIFY = { 'loss' : 'sparse_categorical_crossentropy', 'optimizer' : 'adam', 'activation' : 'relu', 'lastactivation' : 'softmax' }
TENSORFLOWCOMMONCLASSIFYCNN = { 'loss' : 'sparse_categorical_crossentropy', 'optimizer' : 'adam', 'activation' : 'leaky_relu', 'lastactivation' : 'softmax' }
TENSORFLOWCOMMONCLASSIFYCNN2 = { 'loss' : 'sparse_categorical_crossentropy', 'optimizer' : 'adadelta', 'activation' : 'leaky_relu', 'lastactivation' : 'softmax' }
TENSORFLOWCOMMONPREDICT = { 'loss' : 'mse', 'optimizer' : 'rmsprop', 'activation' : 'relu', 'lastactivation' : 'linear', 'inputdropout' : 0, 'dropout' : 0, 'normalize' : False, 'batchnormalize' : False, 'regularize' : False }
TENSORFLOWCOMMONPREDICTCNN = { 'loss' : 'mse', 'optimizer' : 'rmsprop', 'activation' : 'leakyrelu', 'lastactivation' : 'linear' }
TENSORFLOWCOMMON = { 'steps' : 1000, 'lr' : None, 'inputdropout' : 0.25, 'dropout' : 0.2, 'normalize' : True, 'batchnormalize' : True, 'regularize' : True }
TENSORFLOWCOMMONFEED = { 'hidden' : 100, 'layers' : 2 }
TENSORFLOWCOMMONPREFEED = {'hidden' : 100, 'layers': 2, 'convlayers' : 3 }
TENSORFLOWDNNCONFIG = { 'name' : 'dnn', **TENSORFLOWCOMMONFEED, **TENSORFLOWCOMMON }
TENSORFLOWLICCONFIG = { 'name' : 'lic', **TENSORFLOWCOMMON }
TENSORFLOWMLPCONFIG = { 'name' : 'mlp', **TENSORFLOWCOMMONFEED, **TENSORFLOWCOMMON }
TENSORFLOWRNNCONFIG = { 'name' : 'rnn', **TENSORFLOWCOMMONFEED, **TENSORFLOWCOMMON }
TENSORFLOWCNNCONFIG = { 'name' : 'cnn', 'stride' : 1, 'kernelsize' : 4, 'maxpool': 2, **TENSORFLOWCOMMONPREFEED, **TENSORFLOWCOMMON }
TENSORFLOWCNN2CONFIG = { 'name' : 'cnn2', 'stride' : 1, 'kernelsize' : 3, 'maxpool': 2, **TENSORFLOWCOMMONPREFEED, **TENSORFLOWCOMMON }
TENSORFLOWLSTMCONFIG = { 'name' : 'lstm', **TENSORFLOWCOMMONFEED, **TENSORFLOWCOMMON }
TENSORFLOWGRUCONFIG = { 'name' : 'gru', **TENSORFLOWCOMMONFEED, **TENSORFLOWCOMMON }
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

def get(cf, predictor = False, binary = False):
    if cf == TENSORFLOWDNN:
        return cf, 1, TENSORFLOWDNNCONFIG
    elif cf == TENSORFLOWLIC:
        return cf, 2, TENSORFLOWLICCONFIG
    elif cf == TENSORFLOWMLP:
        if predictor:
            TENSORFLOWMLPCONFIG.update(TENSORFLOWCOMMONPREDICT)
        else:
            TENSORFLOWMLPCONFIG.update(TENSORFLOWCOMMONCLASSIFY)
        if binary and not predictor:
            TENSORFLOWMLPCONFIG['loss'] = 'binary_crossentropy'
            TENSORFLOWMLPCONFIG['lastactivation'] = 'sigmoid'
        return cf, 3, TENSORFLOWMLPCONFIG
    elif cf == TENSORFLOWRNN:
        TENSORFLOWRNNCONFIG.update(TENSORFLOWCOMMONCLASSIFY)
        if binary and not predictor:
            TENSORFLOWRNNCONFIG['loss'] = 'binary_crossentropy'
            TENSORFLOWRNNCONFIG['lastactivation'] = 'sigmoid'
        return cf, 4, TENSORFLOWRNNCONFIG
    elif cf == TENSORFLOWCNN:
        TENSORFLOWCNNCONFIG.update(TENSORFLOWCOMMONCLASSIFYCNN)
        if binary and not predictor:
            TENSORFLOWCNNCONFIG['loss'] = 'binary_crossentropy'
            TENSORFLOWCNNCONFIG['lastactivation'] = 'sigmoid'
        return cf, 5, TENSORFLOWCNNCONFIG
    elif cf == TENSORFLOWLSTM:
        TENSORFLOWLSTMCONFIG.update(TENSORFLOWCOMMONCLASSIFY)
        if binary and not predictor:
            TENSORFLOWLSTMCONFIG['loss'] = 'binary_crossentropy'
            TENSORFLOWLSTMCONFIG['lastactivation'] = 'sigmoid'
        return cf, 6, TENSORFLOWLSTMCONFIG
    elif cf == TENSORFLOWGRU:
        TENSORFLOWGRUCONFIG.update(TENSORFLOWCOMMONCLASSIFY)
        if binary and not predictor:
            TENSORFLOWGRUCONFIG['loss'] = 'binary_crossentropy'
            TENSORFLOWGRUCONFIG['lastactivation'] = 'sigmoid'
        return cf, 7, TENSORFLOWGRUCONFIG
    elif cf == TENSORFLOWLIR:
        return cf, 8, TENSORFLOWLIRCONFIG
    elif cf == TENSORFLOWCNN2:
        TENSORFLOWCNN2CONFIG.update(TENSORFLOWCOMMONCLASSIFYCNN2)
        if binary and not predictor:
            TENSORFLOWCNN2CONFIG['loss'] = 'binary_crossentropy'
            TENSORFLOWCNN2CONFIG['lastactivation'] = 'sigmoid'
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
    print("Unknown config", cf)
    return None
