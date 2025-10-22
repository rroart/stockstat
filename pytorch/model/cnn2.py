import torch.nn as nn
import torch
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

        inouts = [ 32, 64, 128 ]
        dims = [ shape[1], *inouts[:config.convlayers], shape[2] ]

        layer1 = nn.ModuleList()
        for i in range(config.convlayers):
            layer1.append(nn.Conv2d(dims[i], dims[i+1], kernel_size = config.kernelsize, stride = config.stride, padding=(config.kernelsize // 2)))
            if self.config.batchnormalize:
                layer1.append(nn.BatchNorm2d(dims[i+1]))
            layer1.append(activation)
            if config.maxpool > 1 and i == (config.convlayers - 1):
                layer1.append(nn.MaxPool2d(kernel_size=config.maxpool))
            if config.dropout > 0:
                layer1.append(nn.Dropout(config.dropout))
        self.layer1 = nn.Sequential(*layer1)
        p1 = self.calc(config, shape[2])
        p2 = self.calc(config, p1)
        q1 = self.calc(config, shape[3])
        q2 = self.calc(config, q1)
        p3 = int(p2 // config.maxpool)
        q3 = int(q2 // config.maxpool)
        print("P12", shape[1], shape[2], p1, p2)
        print(inouts[config.convlayers - 1])
        config.hidden = 64 # todo

        if classify:
            sizearr = [inouts[config.convlayers - 1] * p3 * q3] + [config.hidden] * (config.layers - 1) + [myobj.classes]
        else:
            sizearr = [inouts[config.convlayers - 1] * p3 * q3] + [config.hidden] * (config.layers - 1) + [1]

        mylayers = nn.ModuleList()
        for i in range(0, len(sizearr) - 1):
            #print("sizearr", sizearr[i], sizearr[i+1])
            mylayers.append(nn.Linear(sizearr[i], sizearr[i + 1]))
            if i < (len(sizearr) - 2):
                if config.batchnormalize:
                    mylayers.append(nn.BatchNorm1d(sizearr[i + 1]))
                if i < (len(sizearr) - 3):
                    mylayers.append(activation)
                else:
                    mylayers.append(lastactivation)
                if config.dropout > 0:
                    mylayers.append(nn.Dropout(config.dropout))
            mylayers.append(lastactivation)

        self.fc1 = nn.Sequential(*mylayers)
        self.drop = nn.Dropout(config.dropout)
        self.r1 = activation

        # setup losses
        self.bce = layerutils.getLoss(config)

        # setup optimizer
        self.opt = layerutils.getOptimizer(config, self)
        return

        # prev here
        #https://github.com/yunjey/pytorch-tutorial/blob/master/tutorials/02-intermediate/convolutional_neural_network/main.py
        #print("MO",myobj.size)
        dim1 = shape[1]
        dim2 = shape[2]
        dim3 = shape[3]
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
        #self.layer0 = layerutils.getNormalLayer(shape)
        layer1 = nn.ModuleList()
        layer1.append(nn.Conv2d(dim1, c1, kernel_size = config.kernelsize, stride = config.stride, padding=(config.kernelsize // 2)))
        if self.config.batchnormalize:
            layer1.append(nn.BatchNorm2d(c1))
        layer1.append(activation)
        #nn.MaxPool2d(kernel_size=2, stride=2))
        self.layer1 = nn.Sequential(*layer1)
        layers2 = nn.ModuleList()
        layers2.append(nn.Conv2d(c1, c2, kernel_size = config.kernelsize, stride = config.stride, padding=(config.kernelsize // 2)))
        if self.config.batchnormalize:
            layers2.append(nn.BatchNorm2d(c2))
        layers2.append(activation)
        layers2.append(nn.MaxPool2d(kernel_size=pool_kernel_size))
            #nn.MaxPool2d(kernel_size=4),
            #, stride=2),
        layers2.append(nn.Dropout(config.dropout))
        self.layer2 = nn.Sequential(*layers2)
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
        self.drop = nn.Dropout(config.dropout)
        self.fc15 = nn.Linear(128, 64)
        self.fc2 = nn.Linear(64, myobj.classes)
        self.r1 = activation

        #Defining the layers
        # RNN Layer
        #self.rnn = nn.RNN(self.myobj.size, self.config.hidden, self.config.layers, batch_first=True)
        # Fully connected layer
        #self.fc = nn.Linear(self.config.hidden, self.myobj.classes)

        # setup losses
        self.bce = layerutils.getLoss(config)

        # setup optimizer
        self.opt = layerutils.getOptimizer(config, self)


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
            out = x #self.layer0(x)
        else:
            out =  x
        out = self.layer1(out)
        #print("xxx", out.shape)
        #out = self.layer2(out)
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
            #out = self.fc15(out)
            #out = self.fc2(out)
            ll = 1
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
    
    def calc(self, config, x):
        o1 = (x - config.kernelsize + 2 * (config.kernelsize // 2)) / config.stride + 1
        o1 = int(o1)
        return o1
