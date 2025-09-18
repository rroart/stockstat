import keras

def getNormalLayer(shape):
    if isinstance(shape, tuple):
        shapelen = len(shape)
        print("shapelen", shapelen, shape)
        if shapelen == 2:
            return keras.layers.Normalization(axis=None)
        else:
            return keras.layers.Normalization(axis=shapelen-2)
    return None