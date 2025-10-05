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

        #Defining the layers
        # RNN Layer
        #print("sz",self.myobj.size)
        #print("sz",type(self.myobj.size[0]))
        #print("sz",type(self.myobj.size[1]))
        #print("sz",self.myobj.size)
        print("shape", shape)
        self.rnn = nn.RNN(shape[2], self.config.hidden, self.config.layers, dropout=config.dropout, batch_first=True)
        # Fully connected layer
        if classify:
            self.fc = nn.Linear(self.config.hidden, self.myobj.classes)
        else:
            self.fc = nn.Linear(self.config.hidden, 1)

        self.bn = nn.BatchNorm1d(self.myobj.classes) #shape[1])
        self.act = activation
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
        out = self.act(out)
        if self.config.dropout > 0:
            out = self.dropout(out)

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
    
