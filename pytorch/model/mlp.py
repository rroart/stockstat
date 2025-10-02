import torch.nn as nn
import torch
import torch.nn.functional as F

from model import layerutils


class Net(nn.Module):
    def __init__(self, myobj, config, classify, shape):
        super(Net, self).__init__()

        # Defining some parameters
        self.myobj = myobj
        self.config = config

        print("sh", shape, type(shape))
        if len(shape) > 2:
            raise ValueError("MLP only supports 2D input, shape:" + str(shape))

        activation = layerutils.getActivation(config)
        lastactivation = layerutils.getLastactivation(config)

        if classify:
            sizearr = [shape[1]] + [config.hidden] * config.layers + [myobj.classes]
        else:
            sizearr = [shape[1]] + [config.hidden] * config.layers + [1]

        print(sizearr)

        mylayers = nn.ModuleList()
        #if config.batchnormalize:
        #    mylayers.append(nn.BatchNorm1d(sizearr[0]))
        if config.inputdropout > 0:
            mylayers.append(nn.Dropout(config.inputdropout))
        for i in range(0, len(sizearr) - 1):
            #print("sizearr", sizearr[i], sizearr[i+1])
            mylayers.append(nn.Linear(sizearr[i], sizearr[i + 1]))
            if i < (len(sizearr) - 2):
                if config.batchnormalize:
                    mylayers.append(nn.BatchNorm1d(sizearr[i + 1]))
                if i < (len(sizearr) - 3):
                    mylayers.append(activation)
                else:
                    mylayers.append(lastactivation)
                if config.dropout > 0:
                    mylayers.append(nn.Dropout(config.dropout))
        self.layers = nn.Sequential(*mylayers)
        #self.layers.apply(Xavier)
        
        # setup losses
        self.bce = layerutils.getLoss(config)

        # setup optimizer
        self.opt = layerutils.getOptimizer(config, self)

    def forward(self, x):
        x = self.layers(x)
        return x

    def observe(self, x, y):
        self.train()
        self.opt.zero_grad() #opt
        #print("sz",self.model(x).size(),  y.size())
        loss = self.bce(self(x), y)

        if self.config.regularize:
            regularization_type = 'L2'
            lambda_reg = 0.01
            # Apply L1 regularization
            if regularization_type == 'L1':
                l1_norm = sum(p.abs().sum() for p in self.parameters())
                loss += lambda_reg * l1_norm

            # Apply L2 regularization
            elif regularization_type == 'L2':
                l2_norm = sum(p.pow(2).sum() for p in self.parameters())
                loss += lambda_reg * l2_norm

        loss.backward()
        self.opt.step()
        return loss
