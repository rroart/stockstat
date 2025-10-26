import math

# channel_last, at 1/2

dilation = 1

def calcConvShape(config, xy):
    x = calcConv(config, xy[0])
    if x is None:
        return None
    return (x, xy[1])


# differs
def calcPoolShape(config, xy):
    x = calcPool(config, xy[0])
    if x is None:
        return None
    return (x, xy[1])


def calcConvShape2(config, xyz):
    x = calcConv(config, xyz[0])
    y = calcConv(config, xyz[1])
    if x is None or y is None:
        return None
    return (x, y, xyz[2])


def calcPoolShape2(config, xyz):
    x = calcPool(config, xyz[0])
    y = calcPool(config, xyz[1])
    if x is None or y is None:
        return None
    return (x, y, xyz[2])


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
