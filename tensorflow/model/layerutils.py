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