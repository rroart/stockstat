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

    self.rnn = nn.LSTM(shape[2], self.config.hidden, self.config.layers, dropout=config.dropout, batch_first=True)
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
    batches = x.size(0)
    h0 = torch.zeros([self.config.layers, batches, self.config.hidden]).to(x.device)
    c0 = torch.zeros([self.config.layers, batches, self.config.hidden]).to(x.device)
    (x, _) = self.rnn(x, (h0, c0))
    #c0
    x = x[:,-1,:]  # Keep only the output of the last iteration. Before shape (6,3,50), after shape (6,50)
    #x = nn.relu(x)
    x = self.fc(x)
    if self.config.batchnormalize:
        x = self.bn(x)
    x = self.act(x)
    if self.config.dropout > 0:
        x = self.dropout(x)
    x = self.lastact(x)
    return x
  
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
    
