import torch.nn as nn
import torch
import torch.nn.functional as F

class Net(nn.Module):
    def __init__(self, myobj, config):
        super(Net, self).__init__()

        # Defining some parameters
        self.myobj = myobj
        self.config = config

        self.layers = nn.Sequential(
            nn.Linear(myobj.size, 100),
            nn.ReLU(),
            nn.Linear(100, 100),
            nn.ReLU(),
            nn.Linear(100, myobj.outcomes)
        )

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
