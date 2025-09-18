import torch

def getNormalLayer(shape):
    if isinstance(shape, tuple):
        shapelen = len(shape)
        print("shapelen", shapelen, shape)
        return torch.nn.LayerNorm(shape[shapelen-2:])
    return None