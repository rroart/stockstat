import keras

#keras.layers.preprocessing.Normalization()

def getNormalLayer(shape):
    if isinstance(shape, tuple):
        shapelen = len(shape)
        print("shapelen", shapelen, shape)
        if True:
            return keras.layers.Normalization(axis=None)
        else:
            return keras.layers.Normalization(axis=shapelen-2)
    print("shape not tuple", shape, type(shape))
    return None

def getRegularizer(config):
    if config.regularize:
        regularizer = keras.regularizers.l1_l2(l1=1e-5, l2=1e-4)
    else:
        regularizer = None
    return regularizer
    if config.l2 > 0:
        return keras.regularizers.l2(config.l2)
    elif config.l1 > 0:
        return keras.regularizers.l1(config.l1)
    return None

def getOptimizer(config):
    # "adadelta", "adagrad", "adam", "adamw", "adamax", "ftrl", "nadam", "rmsprop", "sgd", "lion", "lamb", "muon", "loss_scale_optimizer", "adafactor"

    if config.optimizer == 'adam':
        return keras.optimizers.Adam(learning_rate = config.lr)
    elif config.optimizer == 'adadelta':
        return keras.optimizers.Adadelta(learning_rate = config.lr)
    elif config.optimizer == 'adagrad':
        return keras.optimizers.Adagrad(learning_rate = config.lr)
    elif config.optimizer == 'adamw':
        return keras.optimizers.AdamW(learning_rate = config.lr)
    elif config.optimizer == 'adamax':
        return keras.optimizers.Adamax(learning_rate = config.lr)
    elif config.optimizer == 'ftrl':
        return keras.optimizers.Ftrl(learning_rate = config.lr)
    elif config.optimizer == 'nadam':
        return keras.optimizers.Nadam(learning_rate = config.lr)
    elif config.optimizer == 'rmsprop':
        return keras.optimizers.RMSprop(learning_rate = config.lr)
    elif config.optimizer == 'sgd':
        return keras.optimizers.SGD(learning_rate = config.lr)
    elif config.optimizer == 'lion':
        return keras.optimizers.Lion(learning_rate = config.lr)
    elif config.optimizer == 'lamb':
        return keras.optimizers.Lamb(learning_rate = config.lr)
    elif config.optimizer == 'muon':
        return keras.optimizers.Muon(learning_rate = config.lr)
    elif config.optimizer == 'loss_scale_optimizer':
        return keras.optimizers.LossScaleOptimizer
    elif config.optimizer == 'adafactor':
        return keras.optimizers.Adafactor(learning_rate = config.lr)
    print("return default optimizer")
    return keras.optimizers.Adam(learning_rate = config.lr)