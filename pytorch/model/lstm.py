class Model(nn.Module):
  def __init__(self, myobj, config, classify):
    super(Model, self).__init__()
    
    # Defining some parameters
    self.myobj = myobj
    self.config = config
    self.classify = classify
        
    self.rnn = nn.LSTM(self.myobj.size, self.config.hidden, self.config.layers, batch_first=True)   
    # Fully connected layer
    self.fc = nn.Linear(self.config.hidden, self.myobj.classes)

  def forward(self, x):
    batches = x.size(0)
    h0 = torch.zeros([self.config.layers, batches, self.config.hidden])
    c0 = torch.zeros([self.config.layers, batches, self.config.hidden])
    (x, _) = self.lstm(x, (h0, c0))
    x = x[:,-1,:]  # Keep only the output of the last iteration. Before shape (6,3,50), after shape (6,50)
    x = F.relu(x)
    x = self.fc(x)
    return x
