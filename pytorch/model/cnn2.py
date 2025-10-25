import math

import torch.nn as nn
import torch
import torch.nn as nn
import torch

from model import layerutils, cnnutils


# cnn2 used (n, chan, x, y)

class Net(nn.Module):
    def __init__(self, myobj, config, classify, shape):
        super(Net, self).__init__()

        # Defining some parameters
        self.myobj = myobj
        self.config = config
        self.classify = classify

        activation = layerutils.getActivation(config)
        lastactivation = layerutils.getLastactivation(config)

        print("shape", shape)
        inouts = [ 32, 32, 32 ] # todo

        dims = [ shape[1], *inouts[:config.convlayers], shape[2] ]

        d11 = self.calc(config, shape[1])
        d21 = self.calc(config, shape[2])
        d31 = self.calc(config, shape[3])
        d1s = [  ]
        d2s = [  ]
        d3s = [  ]
        if not d11 is None:
            d1s.append(d11)
        if not d21 is None:
            d2s.append(d21)
        if not d31 is None:
            d3s.append(d31)
        for i in range(config.convlayers - 1):
            if len(d1s) > 0:
                ad1 = self.calc(config, d1s[-1])
                if not ad1 is None:
                    d1s.append(ad1)
            if len(d2s) > 0:
                ad2 = self.calc(config, d2s[-1])
                if not ad2 is None:
                    d2s.append(ad2)
            if len(d3s) > 0:
                ad3 = self.calc(config, d3s[-1])
                if not ad3 is None:
                    d3s.append(ad3)
        print("P12", shape, d1s, d2s, d3s)
        print(inouts[config.convlayers - 1])
        curShape = cnnutils.calcConvShape2(config, shape[1:])
        curShape = shape[1:]
        print("curShape", curShape)

        layer1 = nn.ModuleList()
        for i in range(config.convlayers):
            layer1.append(nn.Conv2d(dims[i], dims[i+1], kernel_size = config.kernelsize, stride = config.stride, padding=(config.kernelsize // 2)))
            newShape = cnnutils.calcConvShape2(config, curShape)
            print("newShape", curShape)
            if newShape is None:
                print("newShape none")
                break
            curShape = newShape
            if self.config.batchnormalize:
                layer1.append(nn.BatchNorm2d(dims[i+1]))
            layer1.append(activation)
            #dopool = len(d2s) > i and len(d3s) > i and d2s[i] > 1 and d3s[i] > 1
            #print("bool", len(d1s) > i,len(d2s) > i, d1s[i] > 1, d2s[i] > 1)
            newShape = cnnutils.calcPoolShape2(config, curShape)
            print("newShape2", newShape)
            if newShape is not None and config.maxpool > 1:
                layer1.append(nn.MaxPool2d(kernel_size=config.maxpool))
                curShape = newShape
            if config.dropout > 0:
                layer1.append(nn.Dropout(config.dropout))
        self.layer1 = nn.Sequential(*layer1)

        d23dim = min(len(d2s), len(d3s)) - 1
        if d23dim < 0:
            mlt2 = shape[2]
            mlt3 = shape[3]
        else:
            mlt2 = d2s[d23dim]
            mlt3 = d3s[d23dim]
        if classify:
            sizearr = [inouts[config.convlayers - 1] * curShape[1] * curShape[2]] + [config.hidden] * (config.layers - 1) + [myobj.classes]
        else:
            sizearr = [inouts[config.convlayers - 1] * curShape[1] * curShape[2]] + [config.hidden] * (config.layers - 1) + [1]

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
        d21 = (dim2 - config.kernelsize + 2 * (config.kernelsize // 2)) // config.stride + 1
        #p1 = int(p1)
        d22 = (d21 - config.kernelsize + 2 * (config.kernelsize // 2)) / config.stride + 1
        #p2 = int(p2)
        d31 = (dim3 - config.kernelsize + 2 * (config.kernelsize // 2)) // config.stride + 1
        d32 = (d31 - config.kernelsize + 2 * (config.kernelsize // 2)) / config.stride + 1
        o3 = o2 // pool_kernel_size
        p3 = d22 // pool_kernel_size
        d33 = d32 // pool_kernel_size
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
        self.fc1 = nn.Linear(int(c2 * p3 * d33), 128)
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

    def formula(self, config, x):
        dilation = 1
        padding = config.kernelsize // 2
        x = math.floor((x + 2 * padding - dilation * (config.kernelsize - 1) - 1) / config.stride + 1)
        x = x // config.maxpool
        if x < config.maxpool:
            return None
        return x

    def calc(self, config, x):
        return self.formula(config, x)
        x = (x - config.kernelsize + 2 * (config.kernelsize // 2)) / config.stride + 1
        x = int(x)
        x = x // config.maxpool
        if x < config.maxpool:
            return None
        return x

