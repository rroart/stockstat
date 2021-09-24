import torch.nn as nn
import torch
import torch.nn.functional as F

class Net(nn.Module):
    def __init__(self, myobj, config, classify):
        super(Net, self).__init__()

        # Defining some parameters
        self.myobj = myobj
        self.config = config

        if classify:
            sizearr = [myobj.size] + [config.hidden] * config.layers + [myobj.classes]
        else:
            sizearr = [myobj.size] + [config.hidden] * config.layers + [1]
        mylayers = nn.ModuleList()
        for i in range(0, len(sizearr) - 1):
            #print("sizearr", sizearr[i], sizearr[i+1])
            mylayers.append(nn.Linear(sizearr[i], sizearr[i + 1]))
            if i < (len(sizearr) - 2):
                mylayers.append(nn.ReLU())
        self.layers = nn.Sequential(*mylayers)
        #self.layers.apply(Xavier)
        
        # setup losses
        # setup optimizer
        if classify:
            self.bce = torch.nn.CrossEntropyLoss()
            self.opt = torch.optim.SGD(self.parameters(), lr=config.lr)
        else:
            self.bce = torch.nn.MSELoss()
            self.opt = torch.optim.RMSprop(self.parameters(), lr=config.lr)

    def forward(self, x):
        x = self.layers(x)
        return x

    def observe(self, x, y):
        self.train()
        self.zero_grad()
        #print("sz",self(x).size(),  y.size())
        self.bce(self(x), y).backward()
        self.opt.step()
