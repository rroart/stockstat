import learntest as lt
import os

import torch
import torch.nn as nn
import numpy as np

import json
from datetime import datetime
from werkzeug.wrappers import Response
import shutil

from multiprocessing import Queue

import importlib

global dicteval
dicteval = {}
global dictclass
dictclass = {}
global count
count = 0

class Classify:
    def do_eval(self, request):
        global dicteval
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        print("geteval" + str(myobj.modelInt) + myobj.period + myobj.mapname)
        accuracy_score = dicteval[str(myobj.modelInt) + myobj.period + myobj.mapname]
        return Response(json.dumps({"accuracy": accuracy_score}), mimetype='application/json')

    def do_classify(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        global dictclass
        classifier = dictclass[myobj.mapname]
        del dictclass[myobj.mapname]
        (intlist, problist) = self.do_classifyinner(myobj, classifier)
        print(len(intlist))
        print(intlist)
        print(problist)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        
        return Response(json.dumps({"classifycatarray": intlist, "classifyprobarray": problist }), mimetype='application/json')
        
    def do_classifyinner(self, myobj, model):
        array = np.array(myobj.classifyarray, dtype='f')
        #array = myobj.classifyarray
        intlist = []
        problist = []
        array = torch.FloatTensor(array)
        predictions = model(array)
        print(type(predictions))
        print("predictions")
        print(predictions)
        _, predicted = torch.max(predictions, 1)
        print(_)
        print(predicted)
        #correct = (predicted == labels).sum()
        #accuracy = 100 * correct / total
        #print(accuracy)
        intlist = predicted.tolist()
        #probs = torch.softmax(predictions, dim=1)
        #winners = probs.argmax(dim=1)
        #corrects = (winners == target)
        #clf = np.where(predictions < 0.5, 0, 1)
        #print(clf)
        #for prediction in clf:
        #    intlist.append(int(prediction[0]))
        #    problist.append(int(prediction[0]))
        #shutil.rmtree("/tmp/tf" + str(myobj.modelInt) + myobj.period + myobj.mapname + str(count))
        del predictions
        del model
        return intlist, problist

    def do_learntest(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (modelname, config) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        model = Model.Net(myobj, config)
        accuracy_score = self.do_learntestinner(myobj, model, config)
        global dictclass
        #dictclass[str(myobj.modelInt) + myobj.period + myobj.mapname] = model
        global dicteval
        #dicteval[myobj.mapname] = float(accuracy_score)
        #print("seteval" + myobj.mapname)
        
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        return Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json')

    def mytrain(self, model, inputs, labels, myobj, config):
        labels = labels.long()
        if myobj.modelInt == 2 and not hasattr(myobj, 'trainingcatarray'):
            (inputs, labels) = self.getSlide(inputs, labels, myobj, config)
        t = 0
        v_x = inputs
        v_y = labels
        for i in range(model.config.steps):
            model.train()
            model.observe(v_x, v_y)

    def getSlideOld(self, inputs, labels, myobj):
            #input = torch.from_numpy(np.array([[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20]]]))
            #inputs = torch.from_numpy(inputs)
            size = inputs.size()
            sliding_window_stride = 1
            sliding_window_width = 5
            mysize = size[0]
            minibatch = size[1]
            sequence_length = size[2]
            size2 = labels.size()
            minibatch2 = size2[1]
            length = size2[2]
            arange = int((sequence_length - (sliding_window_width - 1)) / sliding_window_stride)
            splitInput = torch.autograd.Variable(torch.zeros(mysize * arange, minibatch, sliding_window_width))
            splitTarget = torch.autograd.Variable(torch.zeros(mysize * arange, minibatch2, length))
            print(inputs.size())
            print(splitInput.size())
            print(splitTarget.size())
            for j in range(mysize):
                for i in range(arange):
                    print(j*arange+i, j, i*sliding_window_stride, i*sliding_window_stride+sliding_window_width)
                    print(splitInput[j * arange + i].size())
                    print(inputs[j, :, i * sliding_window_stride : (i * sliding_window_stride + sliding_window_width)].size())
                    splitInput[j * arange + i] = inputs[j, :, i * sliding_window_stride : (i * sliding_window_stride + sliding_window_width)]
                    print(splitTarget[j * arange + i].size())
                    print(labels[j, :, :].size())
                    splitTarget[j * arange + i] = labels[j, :, :]
            inputs = splitInput
            labels = splitTarget
            print(inputs)
            print(labels)

    def getSlide(self, inputs, labels, myobj, config):
        #input = torch.from_numpy(np.array([[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20]]]))
        #inputs = torch.from_numpy(inputs)
        size = inputs.size()
        sliding_window_stride = config.slide_stride
        sliding_window_width = myobj.size
        mysize = size[0]
        minibatch = size[1]
        sequence_length = size[2]
        #size2 = labels.size()
        print(size)
        #print(size2)
        #minibatch2 = size2[1]
        #length = size2[2]
        arange = int((sequence_length - (sliding_window_width) - myobj.outcomes) / sliding_window_stride) + 1
        print("arange", arange)
        splitInput = torch.autograd.Variable(torch.zeros(mysize, arange * minibatch, sliding_window_width))
        splitTarget = torch.autograd.Variable(torch.zeros(mysize, arange * minibatch, myobj.outcomes))
        print(inputs.size())
        print(splitInput.size())
        print(splitTarget.size())
        for i in range(mysize):
            for j in range(minibatch):
                for k in range(arange):
                    print(i, j*arange + k, j, k*sliding_window_stride, k*sliding_window_stride+sliding_window_width)
                    #print(splitInput[j * arange + i].size())
                    #print(inputs[j, :, i * sliding_window_stride : (i * sliding_window_stride + sliding_window_width)].size())
                    splitInput[i, j * arange + k] = inputs[i, j, k * sliding_window_stride : (k * sliding_window_stride + sliding_window_width)]
                    #print(splitTarget[i, j * arange].size())
                    #print(labels[j, :, :].size())
                    splitTarget[i, j * arange + k] = inputs[i, j, k * sliding_window_stride + sliding_window_width : (k * sliding_window_stride + sliding_window_width + myobj.outcomes)]
        inputs = splitInput
        labels = splitTarget
        print(inputs)
        print(labels)
        return inputs, labels

    def mytrain2(self, model, inputs, labels, myobj, config):
        if myobj.modelInt == 2 and not hasattr(myobj, 'trainingcatarray'):
            (inputs, labels) = self.getSlide(inputs, labels, myobj, config)
        criterion = nn.BCELoss()
        criterion = nn.CrossEntropyLoss()
        criterion = nn.MSELoss()
        optimizer = torch.optim.SGD(model.parameters(), lr=0.1)
        #, momentum=0.9)
        #labels = torch.autograd.Variable(labels.float())
        
        for i in range(model.config.steps):
            running_loss = 0.0
            # get the inputs; data is a list of [inputs, labels]
            #inputs, labels = data

            # zero the parameter gradients
            optimizer.zero_grad()

            # forward + backward + optimize
            outputs = model.forward(inputs)
            print(inputs.shape)
            print(inputs)
            print(outputs.shape)
            print(outputs)
            #print("jjj", outputs)
            #(max_vals, arg_maxs) = torch.max(outputs.data, dim=1)
            #if i == 0:
            #    print("out", outputs.size(), labels.size(), outputs, labels);
            print(labels.shape)
            print(labels)
            loss = criterion(outputs, labels)
            #print("jjj2")
            loss.backward()
            optimizer.step()

            # print statistics
            running_loss += loss.item()
            
    def do_learntestinner(self, myobj, model, config):
        array = np.array(myobj.trainingarray, dtype='f')
        cat = np.array([], dtype='i')
        if hasattr(myobj, 'trainingcatarray'):
            cat = np.array(myobj.trainingcatarray, dtype='i')
        # NOTE class range 1 - 4 will be changed to 0 - 3
        # to avoid risk of being classified as 0 later
        if not hasattr(myobj, 'zero') or myobj.zero == False:
            cat = cat - 1
        #print(len(cat))
        #print(array)
        #print(cat)
        if hasattr(myobj, 'testarray') and hasattr(myobj, 'testcatarray'):
            test = np.array(myobj.testarray, dtype='f')
            testcat = np.array(myobj.testcatarray, dtype='i')
            train = array
            traincat = cat
            # NOTE class range 1 - 4 will be changed to 0 - 3
            # to avoid risk of being classified as 0 later
            if not hasattr(myobj, 'zero') or myobj.zero == False:
                testcat = testcat - 1
        else:
            (lenrow, lencol) = array.shape
            half = round(lenrow / 2)
            train = array[:half, :]
            test = array[half:, :]
            traincat = cat[:half]
            testcat = cat[half:]
            if len(cat) == 1:
                train = array
                test = array
                traincat = cat
                testcat = cat
        print("outcomes")
        print(myobj.outcomes)
        #print("cwd")
        #print(os.getcwd())
        #np.random.seed(0)
        #torch.manual_seed(0)
        #X, Y = make_moons(500, noise=0.2)
        #X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size=0.25, random_state=73)
        #net = model.network(train, traincat)
        print(model)

        #net.train()

        v_x = torch.FloatTensor(train)
        v_y = torch.LongTensor(traincat).reshape(-1, 1)
        v_y = torch.FloatTensor(traincat)
        #.reshape(-1, 1)
        #print(traincat)
        #print(v_y)
        self.mytrain(model, v_x, v_y, myobj, config)
        model.eval()
        
        test_loss = 0
        accuracy_score = 0

        print("test", test)
        tv_x = torch.FloatTensor(test)
        tv_y = torch.LongTensor(testcat)

        y_hat = model(tv_x)
        (max_vals, arg_maxs) = torch.max(y_hat.data, dim=1)
        print(arg_maxs.size(), arg_maxs)
        print(tv_y.size(), tv_y)
        # arg_maxs is tensor of indices [0, 1, 0, 2, 1, 1 . . ]                 
        num_correct = torch.sum(tv_y==arg_maxs)
        acc = float(num_correct) / len(testcat)
        #y_hat_class = np.where(y_hat.detach().numpy()<0.5, 0, 1)               
        #accuracy = np.sum(tv_y.reshape(-1,1) == y_hat_class) / len(testcat)    
        accuracy_score = acc
 
        #y_hat_class = np.where(y_hat.detach().numpy()<0.5, 0, 1)
        #accuracy = np.sum(testcat.reshape(-1,1) == y_hat_class) / len(testcat)
        #accuracy_score = accuracy
        #train_loss.append(loss.item())

        print("testlen", len(testcat))
        print("test_loss")
        print(test_loss)
        print(accuracy_score)
        print(type(accuracy_score))
        print("\nTest Accuracy: {0:f}\n".format(accuracy_score))
        return accuracy_score

    def getModel(self, myobj):
        if myobj.modelInt == 1:
            modelname = 'mlp'
            config = myobj.pytorchMLPConfig
        if myobj.modelInt == 2:
            modelname = 'rnn'
            config = myobj.pytorchRNNConfig
        return modelname, config
    
    def do_learntestclassify(self, queue, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        #myobj = json.loads(request, object_hook=lt.LearnTest)
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (modelname, config) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        model = Model.Net(myobj, config)
        #myobj.size, myobj.outcomes, dim, layers)
        accuracy_score = self.do_learntestinner(myobj, model, config)
        (intlist, problist) = self.do_classifyinner(myobj, model)
        print(len(intlist))
        print(intlist)
        print(problist)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        queue.put(Response(json.dumps({"classifycatarray": intlist, "classifyprobarray": problist, "accuracy": float(accuracy_score)}), mimetype='application/json'))
