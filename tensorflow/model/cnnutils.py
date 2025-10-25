import math

dilation = 1

def calcConvShape(config, xy):
    y = calcConv(config, xy[1])
    if y is None:
        return None
    return (xy[0], y)


def calcPoolShape(config, xy):
    y = calcPool(config, xy[1])
    if y is None:
        return None
    return (xy[0], y)


def calcConvShape2(config, xyz):
    y = calcConv(config, xyz[1])
    z = calcConv(config, xyz[2])
    if y is None or z is None:
        return None
    return (xyz[0], y, z)


def calcPoolShape2(config, xyz):
    y = calcPool(config, xyz[1])
    z = calcPool(config, xyz[2])
    if y is None or z is None:
        return None
    return (xyz[0], y, z)


def calcConv(config, x):
    padding = config.kernelsize // 2
    x = math.floor((x + 2 * padding - dilation * (config.kernelsize - 1) - 1) / config.stride + 1)
    return x


def calcPool(config, x):
    stride = config.maxpool
    padding = 0  # config.maxpool // 2
    x = math.floor((x + 2 * padding - dilation * (config.maxpool - 1) - 1) / stride + 1)
    if x < config.maxpool:
        return None
    return x
