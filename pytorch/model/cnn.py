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

        #https://github.com/yunjey/pytorch-tutorial/blob/master/tutorials/02-intermediate/convolutional_neural_network/main.py
        #print("MO",myobj.size)
        dim1 = myobj.size[0]
        dim2 = myobj.size[1]
        #print(dim1, dim2)
        c1 = 16
        c2 = 8
        o1 = (dim1 - config.kernelsize + 2 * (config.kernelsize // 2)) / config.stride + 1
        o1 = int(o1)
        o2 = (o1 - config.kernelsize + 2 * (config.kernelsize // 2)) / config.stride + 1
        o2 = int(o2)
        p1 = (dim2 - config.kernelsize + 2 * (config.kernelsize // 2)) / config.stride + 1
        p1 = int(p1)
        p2 = (p1 - config.kernelsize + 2 * (config.kernelsize // 2)) / config.stride + 1
        p2 = int(p2)
        self.layer0 = layerutils.getNormalLayer(myobj.size)
        self.layer1 = nn.Sequential(
            nn.Conv1d(dim1, c1, kernel_size = config.kernelsize, stride = config.stride, padding=(config.kernelsize // 2)),
            nn.BatchNorm1d(c1),
            nn.ReLU(),
            nn.Dropout(config.dropout))
            #nn.MaxPool2d(kernel_size=2, stride=2))
        self.layer2 = nn.Sequential(
            nn.Conv1d(c1, c2, kernel_size = config.kernelsize, stride = config.stride, padding=(config.kernelsize // 2)),
            nn.BatchNorm1d(c2),
            nn.ReLU(),
            nn.Dropout(config.dropout))
        #self.l1 = nn.Conv1d(myobj.size, 16, kernel_size = config.kernelsize, stride = config.stride, padding=(config.kernelsize // 2))
            #nn.MaxPool2d(kernel_size=2, stride=2))
        #self.l2 = nn.Conv1d(16, 32, kernel_size = config.kernelsize, stride = config.stride, padding=(config.kernelsize // 2))
            #nn.MaxPool2d(kernel_size=2, stride=2))
        # self.nlayer1 = nn.Sequential(
        #     nn.Conv2d(1, 16, kernel_size=5, stride=1, padding=2),
        #     nn.BatchNorm2d(16),
        #     nn.ReLU(),
        #     nn.MaxPool2d(kernel_size=2, stride=2))
        # self.nlayer2 = nn.Sequential(
        #     nn.Conv2d(16, 32, kernel_size=5, stride=1, padding=2),
        #     nn.BatchNorm2d(32),
        #     nn.ReLU(),
        #     nn.MaxPool2d(kernel_size=2, stride=2))
        #print("O12", o1, o2)
        #print("P12", p1, p2)
        self.r1 = nn.ReLU()
        self.b1 = nn.BatchNorm1d(64)
        self.fc1 = nn.Linear(c2 * p2, 64)
        self.fc2 = nn.Linear(64, myobj.classes)

        #Defining the layers
        # RNN Layer
        #self.rnn = nn.RNN(self.myobj.size, self.config.hidden, self.config.layers, batch_first=True)   
        # Fully connected layer
        #self.fc = nn.Linear(self.config.hidden, self.myobj.classes)
    
        # setup optimizer
        self.opt = torch.optim.SGD(self.parameters(), lr=config.lr)

        # setup losses
        self.bce = torch.nn.BCELoss()
        if classify:
            self.bce = torch.nn.CrossEntropyLoss()
        else:
            self.bce = torch.nn.MSELoss()

    def forward(self, x):
        
        batch_size = x.size(0)

        # Initializing hidden state for first input using method defined below
        #hidden = self.init_hidden(batch_size)
        #print("hidden", hidden.size(), batch_size)

        # Passing in the input and hidden state into the model and obtaining outputs
        #out, hidden = self.rnn(x, hidden)
        #print("outs", out.size())
        #print("hidden", hidden.size(), batch_size)
        
        # Reshaping the outputs such that it can be fit into the fully connected layer
        #out = out.contiguous().view(-1, self.config.hidden)
        #print("outs", out.size())
        #print("shape")
        #print(x.shape)
        if self.classify and self.config.normalize:
            out = self.layer0(x)
        else:
            out =  x
        out = self.layer1(out)
        out = self.layer2(out)
        
        #r2 = nn.ReLU()
        #out = self.l1(x)
        #bn1 = nn.BatchNorm1d(16)
        #out = bn1(out)
        #out = r1(out)
        #out = self.l2(out)
        #bn2 = nn.BatchNorm1d(32)
        #out = bn2(out)
        #out = r2(out)
        #print("oo", out.shape)

        #print("oo", out.shape)
        out = out.view(out.size(0), -1)
        #print("oo", out.shape)
        out = out.view(out.size(0), -1)
        #print("oo", out.shape)
        out = self.fc1(out)
        #print("oo", out.shape)
        if self.config.batchnormalize:
            out = self.b1(out)
        #print("oo", out.shape)
        out = self.r1(out)
        
        #print("oo", out.shape)
        if self.classify:
            #out = self.fc(out[:, :, -1])
            out = self.fc2(out)
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
    
