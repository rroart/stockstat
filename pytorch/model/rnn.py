import torch.nn as nn
import torch

class Net(nn.Module):
    def __init__(self, myobj, config, classify, shape):
        super(Net, self).__init__()

        # Defining some parameters
        self.myobj = myobj
        self.config = config
        self.classify = classify
        
        #Defining the layers
        # RNN Layer
        #print("sz",self.myobj.size)
        #print("sz",type(self.myobj.size[0]))
        #print("sz",type(self.myobj.size[1]))
        #print("sz",self.myobj.size)
        self.rnn = nn.RNN(self.myobj.size, self.config.hidden, self.config.layers, batch_first=True)
        # Fully connected layer
        if classify:
            self.fc = nn.Linear(self.config.hidden, self.myobj.classes)
        else:
            self.fc = nn.Linear(self.config.hidden, 1)

        self.opt = torch.optim.SGD(self.parameters(), lr=config.lr)
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
    
