import torch.nn as nn
import torch

class Net(nn.Module):
    def __init__(self, myobj, config, classify):
        super(Net, self).__init__()

        # Defining some parameters
        self.myobj = myobj
        self.config = config
        self.classify = classify

        #https://github.com/yunjey/pytorch-tutorial/blob/master/tutorials/02-intermediate/convolutional_neural_network/main.py
        #print("MO",myobj.size)
        dim1 = myobj.size[0]
        dim2 = myobj.size[1]
        dim3 = myobj.size[2]
        c1 = 32
        c2 = 64
        pool_kernel_size = 2
        o1 = (dim1 - config.kernelsize + 2 * (config.kernelsize // 2)) // config.stride + 1
        #o1 = int(o1)
        o2 = (o1 - config.kernelsize + 2 * (config.kernelsize // 2)) // config.stride + 1
        #o2 = int(o2)
        p1 = (dim2 - config.kernelsize + 2 * (config.kernelsize // 2)) // config.stride + 1
        #p1 = int(p1)
        p2 = (p1 - config.kernelsize + 2 * (config.kernelsize // 2)) / config.stride + 1
        #p2 = int(p2)
        q1 = (dim3 - config.kernelsize + 2 * (config.kernelsize // 2)) // config.stride + 1
        q2 = (q1 - config.kernelsize + 2 * (config.kernelsize // 2)) / config.stride + 1
        o3 = o2 // pool_kernel_size
        p3 = p2 // pool_kernel_size
        q3 = q2 // pool_kernel_size
        self.layer1 = nn.Sequential(
            nn.Conv2d(dim1, c1, kernel_size = config.kernelsize, stride = config.stride, padding=(config.kernelsize // 2)),
            nn.BatchNorm2d(c1),
            nn.ReLU())
            #nn.MaxPool2d(kernel_size=2, stride=2))
        self.layer2 = nn.Sequential(
            nn.Conv2d(c1, c2, kernel_size = config.kernelsize, stride = config.stride, padding=(config.kernelsize // 2)),
            nn.BatchNorm2d(c2),
            nn.ReLU(),
            nn.MaxPool2d(kernel_size=pool_kernel_size),
            #nn.MaxPool2d(kernel_size=4),
            #, stride=2),
            nn.Dropout(config.dropout1))
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
        #print("O12", o1, o2, o3)
        #print("P12", p1, p2, p3)
        #print("Q12", q1, q2, q3)
        #pq3 = min(p3, q3)
        #print("pq3", pq3)
        #print(o3, p3, q3)
        #print("fc1", c2, p1, p2, c2*p1*p2)
        #print("fc1", c2, m1, m2, c2*m1*m2)
        # / 2 is the dropout
        #self.fc1 = nn.Linear(int(c2 * m1 * m2 * config.dropout2), 128)
        #self.fc1 = nn.Linear(int(c2 * p1 * p2), 128)
        self.fc1 = nn.Linear(int(c2 * p3 * q3), 128)
        self.drop = nn.Dropout(config.dropout2)
        self.fc15 = nn.Linear(128, 64)
        self.fc2 = nn.Linear(64, myobj.classes)
        self.r1 = nn.ReLU()

        #Defining the layers
        # RNN Layer
        #self.rnn = nn.RNN(self.myobj.size, self.config.hidden, self.config.layers, batch_first=True)   
        # Fully connected layer
        #self.fc = nn.Linear(self.config.hidden, self.myobj.classes)
    
        # setup optimizer
        self.opt = torch.optim.Adadelta(self.parameters(), lr=config.lr)

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
        out = self.layer1(x)
        #print("xxx", out.shape)
        out = self.layer2(out)
        #print("xxx", out.shape)
        
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
        #b1 = nn.BatchNorm2d(64)

        #print("oo", out.shape)
        out = out.view(out.size(0), -1)
        out = out.view(out.size(0), -1)
        #print("xxx", out.shape)
        #print("oo", out.shape)
        out = out.view(out.size(0), -1)
        #print("xxx4", out.shape)
        #print("oo", out.shape)
        out = self.fc1(out)
        #print("oo", out.shape)
        out = self.drop(out)
        #print("oo", out.shape)
        #out = b1(out)
        #print("oo", out.shape)
        out = self.r1(out)
        
        #print("oo", out.shape)
        if self.classify:
            #out = self.fc(out[:, :, -1])
            out = self.fc15(out)
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
    
