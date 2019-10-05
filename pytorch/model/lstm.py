import torch.nn as nn
import torch

class Net(nn.Module):
  def __init__(self, myobj, config, classify):
    super(Net, self).__init__()
    
    # Defining some parameters
    self.myobj = myobj
    self.config = config
    self.classify = classify
        
    self.rnn = nn.LSTM(self.myobj.size, self.config.hidden, self.config.layers, batch_first=True)   
    # Fully connected layer
    self.fc = nn.Linear(self.config.hidden, self.myobj.classes)

    # setup optimizer
    self.opt = torch.optim.SGD(self.parameters(), lr=config.lr)

    # setup losses
    self.bce = torch.nn.BCELoss()
    if classify:
      self.bce = torch.nn.CrossEntropyLoss()
    else:
      self.bce = torch.nn.MSELoss()

  def forward(self, x):
    batches = x.size(0)
    h0 = torch.zeros([self.config.layers, batches, self.config.hidden])
    c0 = torch.zeros([self.config.layers, batches, self.config.hidden])
    (x, _) = self.rnn(x, (h0, c0))
    #c0
    x = x[:,-1,:]  # Keep only the output of the last iteration. Before shape (6,3,50), after shape (6,50)
    #x = nn.relu(x)
    x = self.fc(x)
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
    
