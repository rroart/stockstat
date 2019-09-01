import torch.nn as nn
import torch
import torch.nn.functional as F

class Net(nn.Module):
    def __init__(self, myobj, config, classify):
        super(Net, self).__init__()

        # Defining some parameters
        self.myobj = myobj
        self.config = config

        sizearr = [myobj.size] + [config.hiddenunits] * config.hiddenlayers + [myobj.classes]
        mylayers = []
        for i in range(0, len(sizearr) - 1):
            print("sizearr", sizearr[i], sizearr[i+1])
            mylayers.append(nn.Linear(sizearr[i], sizearr[i + 1]))
            if i < (len(sizearr) - 2):
                mylayers.append(nn.ReLU())
        self.layers = nn.Sequential(*mylayers)
        #self.layers.apply(Xavier)
        
        # setup optimizer
        self.opt = torch.optim.SGD(self.parameters(), lr=config.lr)

        # setup losses
        self.bce = torch.nn.CrossEntropyLoss()

    def forward(self, x):
        x = self.layers(x)
        return x

    def observe(self, x, y):
        self.train()
        self.zero_grad()
        self.bce(self(x), y).backward()
        self.opt.step()
