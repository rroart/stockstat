import torch.nn as nn
import torch

class Net(nn.Module):
  def __init__(self, myobj, config, classify, shape):
    super(Net, self).__init__()
    
    # Defining some parameters
    self.myobj = myobj
    self.config = config
    self.classify = classify
    
    self.rnn = nn.LSTM(self.myobj.size, self.config.hidden, self.config.layers, batch_first=True)   
    # Fully connected layer
    if classify:
      self.fc = nn.Linear(self.config.hidden, self.myobj.classes)
    else:
      self.fc = nn.Linear(self.config.hidden, 1)

    # setup losses
    # setup optimizer
    self.bce = torch.nn.BCELoss()
    if classify:
      self.bce = torch.nn.CrossEntropyLoss()
      self.opt = torch.optim.SGD(self.parameters(), lr=config.lr)
    else:
      self.bce = torch.nn.MSELoss()
      self.opt = torch.optim.RMSprop(self.parameters(), lr=config.lr)

  def forward(self, x):
    batches = x.size(0)
    h0 = torch.zeros([self.config.layers, batches, self.config.hidden]).to(x.device)
    c0 = torch.zeros([self.config.layers, batches, self.config.hidden]).to(x.device)
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
    
