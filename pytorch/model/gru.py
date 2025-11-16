import torch.nn as nn
import torch

from model import layerutils


class Net(nn.Module):
    def __init__(self, myobj, config, classify, shape):
        super(Net, self).__init__()

        # Defining some parameters
        self.myobj = myobj
        self.config = config
        self.classify = classify

        activation = layerutils.getActivation(config)
        lastactivation = layerutils.getLastactivation(config)
        if config.binary:
            myobj.classes = 1

        #Defining the layers
        # RNN Layer
        self.rnn = nn.GRU(shape[2], self.config.hidden, self.config.layers, dropout=config.dropout, batch_first=True)
        # Fully connected layer
        if classify:
            self.fc = nn.Linear(self.config.hidden, self.myobj.classes)
        else:
            self.fc = nn.Linear(self.config.hidden, 1)

        self.bn = nn.BatchNorm1d(self.myobj.classes) #shape[1])
        self.act = activation
        self.lastact = lastactivation
        self.dropout = nn.Dropout(config.dropout)

        # setup losses
        self.bce = layerutils.getLoss(config)

        # setup optimizer
        self.opt = layerutils.getOptimizer(config, self)


    def forward(self, x):
        
        batch_size = x.size(0)

        # Initializing hidden state for first input using method defined below
        hidden = self.init_hidden(batch_size).to(x.device)
        #print("hidden", hidden.size(), batch_size)

        # Passing in the input and hidden state into the model and obtaining outputs
        out, hidden = self.rnn(x, hidden)
        #print("outs", out.size())
        #print("hidden", hidden.size(), batch_size)
        
        # Reshaping the outputs such that it can be fit into the fully connected layer
        #out = out.contiguous().view(-1, self.config.hidden)
        #print("outs", out.size())
        if self.classify:
            out = self.fc(out[:, -1, :])
        else:
            out = self.fc(out)
        #print("outs", out.size())
        if self.config.batchnormalize:
            out = self.bn(out)
        if self.config.activation:
            out = self.act(out)
        if self.config.dropout > 0:
            out = self.dropout(out)
        if self.config.lastactivation:
            out = self.lastact(out)

        return out
    #, hidden
    
    def init_hidden(self, batch_size):
        # This method generates the first hidden state of zeros which we'll use in the forward pass
        # We'll send the tensor holding the hidden state to the device we specified earlier as well
        hidden = torch.zeros(self.config.layers, batch_size, self.config.hidden)
        return hidden


    def observe(self, x, y):
        if self.classify:
            y = y
        else:
            y = y.float()
        self.train()
        self.zero_grad()
        #print("sz",self(x).size(),  y.size())
        self.bce(self(x), y).backward()
        self.opt.step()
    
