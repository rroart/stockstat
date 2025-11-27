import torch
import numpy as np

def getNormalLayer(shape):
    if isinstance(shape, tuple):
        shapelen = len(shape)
        print("shapelen", shapelen, shape)
        return torch.nn.LayerNorm(shape)
    print("shape not tuple", shape, type(shape))
    return None


def avgstdvar(array):
    r1 = np.average(array)
    #print("\nMean: ", r1)

    r2 = np.sqrt(np.mean((array - np.mean(array)) ** 2))
    #print("\nstd: ", r2)

    r3 = np.mean((array - np.mean(array)) ** 2)
    #print("\nvariance: ", r3)
    return (r1, r2, r3)


def normalize(input):
    (mean, std, var) = avgstdvar(input.numpy())
    x = input / 1  # 255.0
    x = input - mean
    x = x / std
    return x

def getLoss(config):
    # "l1", "mse", "cross_entropy", "ctc", "nl", "poissonnl", "gaussiannl", "kl", "bce", "bcewithlogits", "marginrank", "hingeembedding", "multilabelmargin", "multilabelsoftmargin", "cosineembedding", "multimargin", "tripletmargin", "tripletmarginwithdistance"
    if config.loss == "l1":
        return torch.nn.L1Loss()
    elif config.loss == "mse":
        return torch.nn.MSELoss()
    elif config.loss == "cross_entropy":
        return torch.nn.CrossEntropyLoss()
    elif config.loss == "ctc":
        return torch.nn.CTCLoss()
    elif config.loss == "nl":
        return torch.nn.NLLLoss()
    elif config.loss == "poissonnl":
        return torch.nn.PoissonNLLLoss()
    elif config.loss == "gaussiannl":
        return torch.nn.GaussianNLLLoss()
    elif config.loss == "kl":
        return torch.nn.KLDivLoss()
    elif config.loss == "bce":
        return torch.nn.BCELoss()
    elif config.loss == "bcewithlogits":
        return torch.nn.BCEWithLogitsLoss()
    elif config.loss == "marginrank":
        return torch.nn.MarginRankingLoss()
    elif config.loss == "hingeembedding":
        return torch.nn.HingeEmbeddingLoss()
    elif config.loss == "multilabelmargin":
        return torch.nn.MultiLabelMarginLoss()
    elif config.loss == "multilabelsoftmargin":
        return torch.nn.MultiLabelSoftMarginLoss()
    elif config.loss == "cosineembedding":
        return torch.nn.CosineEmbeddingLoss()
    elif config.loss == "multimargin":
        return torch.nn.MultiMarginLoss()
    elif config.loss == "tripletmargin":
        return torch.nn.TripletMarginLoss()
    elif config.loss == "tripletmarginwithdistance":
        return torch.nn.TripletMarginWithDistanceLoss()
    elif config.loss == "huber":
        return torch.nn.HuberLoss()
    print("return default loss")
    return None
    return torch.nn.BCELoss()

def getOptimizer(config, model):
     # "adadelta", "adafactor", "adagrad", "adam", "adamw", "sparseadam", "adamax", "asgd", "lbfgs", "nadam",
     #             "radam", "rmsprop", "rprop", "sgd", "lr_scheduler", "lambda_lr", "multiplicatative_lr", "step_lr",
     #             "multistep_lr", "constant_lr", "linear_lr", "exponential_lr", "polynomial_lr", "cosine_annealing_lr",
     #             "chained_scheduler", "sequential_lr", "reduce_lr_on_plateau", "cyclic_lr", "one_cycle_lr",
     #             "cosine_annealing_warm_restarts", "averaged_model", "swa_lr"
     varargs = dict()
     if config.lr is not None:
         varargs["lr"] = config.lr
     print("opt lr", config.optimizer, varargs)

     if config.optimizer == "adadelta":
         return torch.optim.Adadelta(model.parameters(), **varargs)
     elif config.optimizer == "adafactor":
         return torch.optim.Adafactor(model.parameters(), **varargs)
     elif config.optimizer == "adagrad":
         return torch.optim.Adagrad(model.parameters(), **varargs)
     elif config.optimizer == "adam":
         return torch.optim.Adam(model.parameters(), **varargs)
     elif config.optimizer == "adamw":
         return torch.optim.AdamW(model.parameters(), **varargs)
     elif config.optimizer == "sparseadam":
         return torch.optim.SparseAdam(model.parameters(), **varargs)
     elif config.optimizer == "adamax":
         return torch.optim.Adamax(model.parameters(), **varargs)
     elif config.optimizer == "asgd":
         return torch.optim.ASGD(model.parameters(), **varargs)
     elif config.optimizer == "lbfgs":
         return torch.optim.LBFGS(model.parameters(), **varargs)
     elif config.optimizer == "nadam":
         return torch.optim.NAdam(model.parameters(), **varargs)
     elif config.optimizer == "radam":
         return torch.optim.RAdam(model.parameters(), **varargs)
     elif config.optimizer == "rmsprop":
         return torch.optim.RMSprop(model.parameters(), **varargs)
     elif config.optimizer == "rprop":
         return torch.optim.Rprop(model.parameters(), **varargs)
     elif config.optimizer == "sgd":
         return torch.optim.SGD(model.parameters(), **varargs)
     elif config.optimizer == "lr_scheduler":
         return torch.optim.lr_scheduler.LRScheduler(model.parameters(), **varargs)
     elif config.optimizer == "lambda_lr":
         return torch.optim.lr_scheduler.LambdaLR(model.parameters(), **varargs)
     elif config.optimizer == "multiplicatative_lr":
         return torch.optim.lr_scheduler.MultiplicativeLR(model.parameters(), **varargs)
     elif config.optimizer == "step_lr":
         return torch.optim.lr_scheduler.StepLR(model.parameters(), **varargs)
     elif config.optimizer == "multistep_lr":
         return torch.optim.lr_scheduler.MultiStepLR(model.parameters(), **varargs)
     elif config.optimizer == "constant_lr":
         return torch.optim.lr_scheduler.ConstantLR(model.parameters(), **varargs)
     elif config.optimizer == "linear_lr":
         return torch.optim.lr_scheduler.LinearLR(model.parameters(), **varargs)
     elif config.optimizer == "exponential_lr":
         return torch.optim.lr_scheduler.ExponentialLR(model.parameters(), **varargs)
     elif config.optimizer == "polynomial_lr":
         return torch.optim.lr_scheduler.PolynomialLR(model.parameters(), **varargs)
     elif config.optimizer == "cosine_annealing_lr":
         return torch.optim.lr_scheduler.CosineAnnealingLR(model.parameters(), **varargs)
     elif config.optimizer == "chained_scheduler":
         return torch.optim.lr_scheduler.ChainedScheduler(model.parameters(), **varargs)
     elif config.optimizer == "sequential_lr":
         return torch.optim.lr_scheduler.SequentialLR(model.parameters(), **varargs)
     elif config.optimizer == "reduce_lr_on_plateau":
         return torch.optim.lr_scheduler.ReduceLROnPlateau(model.parameters(), **varargs)
     elif config.optimizer == "cyclic_lr":
         return torch.optim.lr_scheduler.CyclicLR(model.parameters(), **varargs)
     elif config.optimizer == "one_cycle_lr":
         return torch.optimlr_scheduler.OneCycleLR(model.parameters(), **varargs)
     elif config.optimizer == "cosine_annealing_warm_restarts":
         return torch.optim.lr_scheduler.CosineAnnealingWarmRestarts(model.parameters(), **varargs)
     elif config.optimizer == "averaged_model":
         return torch.optim.swa_utils.AveragedModel(model.parameters(), **varargs)
     elif config.optimizer == "swa_lr":
         return torch.optim.swa_utils.SWALR(model.parameters(), **varargs)
     print("return default optimizer")
     return None
     return torch.optim.SGD(model.parameters(), **varargs)

def getAnActivation(activation):
    # "elu", "hard_shrink", "hard_sigmoid", "hard_tanh", "hard_swish", "leakyrelu", "log_sigmoid", "multihead_attention", "prelu", "relu", "relu6", "rrelu", "selu", "celu", "gelu", "sigmoid", "silu", "mish", "soft_plus", "soft_shrink", "soft_sign", "tanh", "tanh_shrink", "threshold", "glu", "soft_min", "soft_max", "soft_max_2d", "log_soft_max", "adaptive_log_soft_max_with_loss"
    if activation == "elu":
        return torch.nn.ELU()
    elif activation == "hard_shrink":
        return torch.nn.Hardshrink()
    elif activation == "hard_sigmoid":
        return torch.nn.Hardsigmoid()
    elif activation == "hard_tanh":
        return torch.nn.Hardtanh()
    elif activation == "hard_swish":
        return torch.nn.Hardswish()
    elif activation == "leakyrelu":
        return torch.nn.LeakyReLU()
    elif activation == "log_sigmoid":
        return torch.nn.LogSigmoid()
    elif activation == "multihead_attention":
        return torch.nn.MultiheadAttention()
    elif activation == "prelu":
        return torch.nn.PReLU()
    elif activation == "relu":
        return torch.nn.ReLU()
    elif activation == "relu6":
        return torch.nn.ReLU6()
    elif activation == "rrelu":
        return torch.nn.RReLU()
    elif activation == "selu":
        return torch.nn.SELU()
    elif activation == "celu":
        return torch.nn.CELU()
    elif activation == "gelu":
        return torch.nn.GELU()
    elif activation == "sigmoid":
        return torch.nn.Sigmoid()
    elif activation == "silu":
        return torch.nn.SiLU()
    elif activation == "mish":
        return torch.nn.Mish()
    elif activation == "soft_plus":
        return torch.nn.Softplus()
    elif activation == "soft_shrink":
        return torch.nn.Softshrink()
    elif activation == "soft_sign":
        return torch.nn.Softsign()
    elif activation == "tanh":
        return torch.nn.Tanh()
    elif activation == "tanh_shrink":
        return torch.nn.Tanhshrink()
    elif activation == "threshold":
        return torch.nn.Threshold()
    elif activation == "glu":
        return torch.nn.GLU()
    elif activation == "soft_min":
        return torch.nn.Softmin()
    elif activation == "softmax":
        return torch.nn.Softmax(dim=1)
    elif activation == "soft_max_2d":
        return torch.nn.Softmax2d()
    elif activation == "log_soft_max":
        return torch.nn.LogSoftmax()
    elif activation == "adaptive_log_soft_max_with_loss":
        return torch.nn.AdaptiveLogSoftmaxWithLoss()
    elif activation == "linear":
        return torch.nn.Identity()
    print("return default activation for", activation)
    return None

def getActivation(config):
    return getAnActivation(config.activation)

def getLastactivation(config):
    return getAnActivation(config.lastactivation)
