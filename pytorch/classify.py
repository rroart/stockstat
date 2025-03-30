import learntest as lt
import os

import torch
import torch.nn as nn
import numpy as np

import json
from datetime import datetime
from werkzeug.wrappers import Response
from werkzeug.utils import secure_filename
import shutil

#from multiprocessing import Queue

import importlib

import mydatasets
# NONO from datasetcli import dataset

global dicteval
dicteval = {}
global dictclass
dictclass = {}
global count
count = 0

print("Using gpu: ", torch.cuda.is_available())

class Classify:
    def do_eval(self, request):
        global dicteval
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        print("geteval" + str(myobj.modelInt) + myobj.period + myobj.modelname)
        accuracy_score = dicteval[str(myobj.modelInt) + myobj.period + myobj.modelname]
        return Response(json.dumps({"accuracy": accuracy_score}), mimetype='application/json')

    def do_classify(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        classify = not hasattr(myobj, 'classify') or myobj.classify == True
        global dictclass
        classifier = dictclass[myobj.modelname]
        del dictclass[myobj.modelname]
        (intlist, problist) = self.do_classifyinner(myobj, classifier, classify)
        print(len(intlist))
        #print(intlist)
        #print(problist)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        
        return Response(json.dumps({"classifycatarray": intlist, "classifyprobarray": problist }), mimetype='application/json')
        
    def do_classifyinner(self, myobj, model, classify):
        dev = self.getdev()
        array = np.array(myobj.classifyarray, dtype='f')
        #array = myobj.classifyarray
        intlist = []
        problist = []
        array = torch.FloatTensor(array).to(dev)
        #print("rrayshape")
        #print(array.shape)
        #probs = torch.softmax(predictions, dim=1)
        #winners = probs.argmax(dim=1)
        #corrects = (winners == target)
        #clf = np.where(predictions < 0.5, 0, 1)
        #print(clf)
        #for prediction in clf:
        #    intlist.append(int(prediction[0]))
        #    problist.append(int(prediction[0]))
        #shutil.rmtree("/tmp/tf" + str(myobj.modelInt) + myobj.period + myobj.modelname + str(count))
        if not classify:
            #print("l",len(array))
            #print("ar", array)
            if myobj.modelInt == 1:
                predicted = torch.empty((len(array), 0)).to(dev)
            else:
                predicted = torch.empty((len(array), 1, 0)).to(dev)
            #print("pr", predicted)
            #print(myobj.size, myobj.classes)
            #print("pred", predicted.shape, predicted)
            for i in range(myobj.classes):
                #print("i", i)
                #print("ashape", array.shape)
                predictions = model(array)
                #print(type(predictions))
                #print("predictions")
                #print(predictions)
                _, mypredicted = torch.max(predictions, 1)
                #print("_", _)
                #print("m", mypredicted)
                intlist = _.tolist()
                intlist = np.array(intlist)
                intlist = torch.FloatTensor(intlist).to(dev)
                #print(type(predicted), predicted.shape)
                if myobj.modelInt == 1:
                    #print(intlist.shape)
                    intlist2 = intlist.reshape(intlist.shape[0], 1)
                    #print(array.shape)
                    #print(intlist2.shape)
                    array = torch.cat((array[:,1:], intlist2), 1)
                    #print(predicted.shape)
                    predicted = torch.cat((predicted, intlist2), 1)
                else:
                    intlist3 = intlist.reshape(intlist.shape[0], 1, 1)
                    array = torch.cat((array[:,:,1:], intlist3), 2)
                    predicted = torch.cat((predicted, intlist3), 2)
            if not myobj.modelInt == 1:
                predicted = predicted.reshape(predicted.shape[0], predicted.shape[2])
            #print("predarray", predicted.shape, predicted)
            predicted = predicted.tolist()
            return predicted, problist
        else:
            print("Classify", array.shape)
            predictions = model(array)
            #print(type(predictions))
            #print("predictions")
            #print(predictions)
            _, predicted = torch.max(predictions, 1)
            #print(_)
            #print(predicted)
            #correct = (predicted == labels).sum()
            #accuracy = 100 * correct / total
            #print(accuracy)
            intlist = predicted.tolist()
            sm = torch.nn.Softmax(1)
            probabilities = sm(predictions)
            probability, _ = torch.max(probabilities, 1)
            problist = probability.detach().to(torch.device("cpu")).numpy().tolist()
            problist = np.where(np.isnan(problist), None, problist).tolist()
        del predictions
        del model
        if classify and not self.zero(myobj):
            intlist = np.array(intlist)
            intlist = intlist + 1
            intlist = intlist.tolist()
        return intlist, problist

    def do_learntest(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        classify = not hasattr(myobj, 'classify') or myobj.classify == True

        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, size) = self.gettraintest(myobj, config, classify)
        myobj.size = size
        model = Model.Net(myobj, config, classify)
        if torch.cuda.is_available():
            model.cuda()
        #testcat = torch.LongTensor(testcat)
        (accuracy_score, loss, train_accuracy_score) = self.do_learntestinner(myobj, model, config, train, traincat, test, testcat, classify)
        global dictclass
        #dictclass[str(myobj.modelInt) + myobj.period + myobj.modelname] = model
        global dicteval
        #dicteval[myobj.modelname] = float(accuracy_score)
        #print("seteval" + myobj.modelname)
        
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        return Response(json.dumps({"accuracy": float(accuracy_score), "trainaccuracy": float(train_accuracy_score)}), mimetype='application/json')

    def mytrain(self, model, inputs, labels, myobj, config, classify):
        if classify:
            labels = labels.long()
        v_x = inputs
        dev = self.getdev()
        v_y = labels.to(dev)
        for i in range(model.config.steps):
            model.train()
            #print(v_x.shape)
            model.observe(v_x, v_y)
            torch.cuda.empty_cache()

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
            #print(inputs.size())
            #print(splitInput.size())
            #print(splitTarget.size())
            for j in range(mysize):
                for i in range(arange):
                    #print(j*arange+i, j, i*sliding_window_stride, i*sliding_window_stride+sliding_window_width)
                    #print(splitInput[j * arange + i].size())
                    #print(inputs[j, :, i * sliding_window_stride : (i * sliding_window_stride + sliding_window_width)].size())
                    splitInput[j * arange + i] = inputs[j, :, i * sliding_window_stride : (i * sliding_window_stride + sliding_window_width)]
                    #print(splitTarget[j * arange + i].size())
                    #print(labels[j, :, :].size())
                    splitTarget[j * arange + i] = labels[j, :, :]
            inputs = splitInput
            labels = splitTarget
            #print(inputs)
            #print(labels)

    def getSlide2(self, inputs, labels, myobj, config):
        #input = torch.from_numpy(np.array([[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20]]]))
        #inputs = torch.from_numpy(inputs)
        inputs = torch.FloatTensor(inputs)
        size = inputs.size()
        sliding_window_stride = config.slide_stride
        sliding_window_width = myobj.size
        #print(type(size))
        mysize = size[0]
        minibatch = size[1]
        sequence_length = size[2]
        #size2 = labels.size()
        #print(size)
        #print(size2)
        #minibatch2 = size2[1]
        #length = size2[2]
        arange = int((sequence_length - (sliding_window_width) - myobj.classes) / sliding_window_stride) + 1
        #print("arange", arange)
        splitInput = torch.autograd.Variable(torch.zeros(arange * mysize, minibatch, sliding_window_width))
        splitTarget = torch.autograd.Variable(torch.zeros(arange * mysize, minibatch, myobj.classes))
        #was:
        #splitInput = torch.autograd.Variable(torch.zeros(mysize, arange * minibatch, sliding_window_width))
        #splitTarget = torch.autograd.Variable(torch.zeros(mysize, arange * minibatch, myobj.classes))
        #print(inputs.size())
        #print(splitInput.size())
        #print(splitTarget.size())
        # [seq_length, batch_size, features],
        for i in range(mysize):
            for j in range(minibatch):
                for k in range(arange):
                    #print(i, j*arange + k, j, k*sliding_window_stride, k*sliding_window_stride+sliding_window_width)
                    #print(splitInput[j * arange + i].size())
                    #print(inputs[j, :, i * sliding_window_stride : (i * sliding_window_stride + sliding_window_width)].size())
                    #print(splitTarget[i, j * arange].size())
                    #print(labels[j, :, :].size())
                    splitInput[i * arange + k, j] = inputs[i, j, k * sliding_window_stride : (k * sliding_window_stride + sliding_window_width)]
                    #splitTarget[i * arange + k, j] = inputs[i, j, k * sliding_window_stride + 1 : (k * sliding_window_stride + 1 + 1)]
                    splitTarget[i * arange + k, j] = inputs[i, j, k * sliding_window_stride + sliding_window_width : (k * sliding_window_stride + sliding_window_width + myobj.classes)]
                    #was:
                    #splitInput[i, j * arange + k] = inputs[i, j, k * sliding_window_stride : (k * sliding_window_stride + sliding_window_width)]
                    #splitTarget[i, j * arange + k] = inputs[i, j, k * sliding_window_stride + sliding_window_width : (k * sliding_window_stride + sliding_window_width + myobj.classes)]
        inputs = splitInput
        labels = splitTarget
        #print(inputs)
        #print(labels)
        return inputs, labels

    def getSlide(self, inputs, labels, myobj, config):
        dev = self.getdev()
        #input = torch.from_numpy(np.array([[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20]]]))
        #inputs = torch.from_numpy(inputs)
        inputs = torch.FloatTensor(inputs).to(dev)
        size = inputs.size()
        if  hasattr(config, 'slide_stride'):
            sliding_window_stride = config.slide_stride
        else:
            sliding_window_stride = 1
        sliding_window_width = myobj.size
        #print(type(size))
        #print(size)
        mysize = size[0]
        sequence_length = size[1]
        arange = int((sequence_length - (sliding_window_width) - myobj.classes) / sliding_window_stride) + 1
        #print("arange", arange, sequence_length, sliding_window_width, myobj.classes, sliding_window_stride)
        #print("arange", arange)
        splitInput = torch.autograd.Variable(torch.zeros(mysize * arange, sliding_window_width))
        splitTarget = torch.autograd.Variable(torch.zeros(mysize * arange))
        #print(inputs.size())
        #print(splitInput.size())
        #print(splitTarget.size())
        # [seq_length, batch_size, features],
        for i in range(mysize):
          for j in range(arange):
            splitInput[i * arange + j] = inputs[i, j * sliding_window_stride : (j * sliding_window_stride + sliding_window_width)]
            splitTarget[i * arange + j] = inputs[i, j * sliding_window_stride + sliding_window_width]
        inputs = splitInput
        labels = splitTarget
        #myobj.classes = 1
        if config.name == 'mlp':
            ii = 1
        else:
            #print("notmlp")
            #print(splitInput.shape)
            #myobj.size = (1, sliding_window_width)
            inputs = inputs.reshape(mysize * arange, 1, sliding_window_width)
            #print(splitInput.shape)
        labels = torch.FloatTensor(labels)
        #print(inputs)
        #print(labels)
        print("iii", inputs.shape)
        print("lll", labels.shape)
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
            #print(inputs.shape)
            #print(inputs)
            #print(outputs.shape)
            #print(outputs)
            #print("jjj", outputs)
            #(max_vals, arg_maxs) = torch.max(outputs.data, dim=1)
            #if i == 0:
            #    print("out", outputs.size(), labels.size(), outputs, labels);
            #print(labels.shape)
            #print(labels)
            loss = criterion(outputs, labels)
            #print("jjj2")
            loss.backward()
            optimizer.step()

            # print statistics
            running_loss += loss.item()

    # non-zero is default
    def zero(self, myobj):
        return hasattr(myobj, 'zero') and myobj.zero == True
            
    def gettraintest(self, myobj, config, classify):
        mydim = myobj.size
        array = np.array(myobj.trainingarray, dtype='f')
        cat = np.array([], dtype='i')
        if hasattr(myobj, 'trainingcatarray') and not myobj.trainingcatarray is None:
            cat = np.array(myobj.trainingcatarray, dtype='i')
        # NOTE class range 1 - 4 will be changed to 0 - 3
        # to avoid risk of being classified as 0 later
        if not self.zero(myobj):
            cat = cat - 1
        #print(len(cat))
        #print(array)
        #print(cat)
        if not classify:
            (inputs, labels) = self.getSlide(array, None, myobj, config)
            mydim = myobj.size
            return inputs, labels, inputs, labels, mydim
        if hasattr(myobj, 'testarray') and hasattr(myobj, 'testcatarray'):
            test = np.array(myobj.testarray, dtype='f')
            testcat = np.array(myobj.testcatarray, dtype='i')
            train = array
            traincat = cat
            # NOTE class range 1 - 4 will be changed to 0 - 3
            # to avoid risk of being classified as 0 later
            if not self.zero(myobj):
                testcat = testcat - 1
        else:
            lenrow = array.shape[0]
            half = round(lenrow / 2)
            # toto
            # too many indices for array: array is 1-dimensional, but 2 were inde
            #print("si ", half)
            #print("si ", array)
            #print("si ", lenrow)
            train = array[:half, :]
            test = array[half:, :]
            #print(train)
            #print(test)
            traincat = cat[:half]
            testcat = cat[half:]
            if len(cat) == 1:
                train = array
                test = array
                traincat = cat
                testcat = cat

        #print("mydim", mydim)
        #print (train.shape)
        if config.name == "cnn" or config.name == "cnn2":
            mydim = train.shape[1:]
            #print("mydim2", mydim)
        #print("mydim", mydim)
        print("Shapes", train.shape, traincat.shape, mydim)
        return train, traincat, test, testcat, mydim
        #print("classes")
        #print(myobj.classes)
        #print("cwd")
        #print(os.getcwd())
        #np.random.seed(0)
        #torch.manual_seed(0)
        #X, Y = make_moons(500, noise=0.2)
        #X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size=0.25, random_state=73)
        #net = model.network(train, traincat)
        
    def do_learntestinner(self, myobj, model, config, train, traincat, test, testcat, classify):
        print(model)
        dev = self.getdev()
        print("Device", dev)

        #net.train()

        v_x = torch.FloatTensor(train).to(dev)
        #v_y = torch.LongTensor(traincat).reshape(-1, 1)
        v_y = torch.FloatTensor(traincat).to(dev)
        #.reshape(-1, 1)
        #print(traincat)
        #print(v_y)
        #print(v_x.size(), v_y.size())
        self.mytrain(model, v_x, v_y, myobj, config, classify)
        model.eval()

        if not classify:
            tv_x = torch.FloatTensor(test).to(dev)
            #print("test", test)
            y_hat = model(tv_x)
            loss = model.bce(y_hat, testcat.to(dev))
            #print(testcat)
            tv_y = torch.FloatTensor(testcat).to(dev)
            #print("losss", loss.item())
            #print(tv_y.size(), tv_y)
            #print(y_hat.size(),y_hat)
            return None, loss.item(), None
            
        test_loss = 0
        accuracy_score = 0
        train_accuracy_score = 0

        y0_hat = model(v_x)
        (max_vals0, arg_maxs0) = torch.max(y0_hat.data, dim=1)
        num_correct0 = torch.sum(v_y==arg_maxs0.to(dtype=torch.float32))
        acc0 = float(num_correct0) / len(v_y)
        
        #print("test", len(test), len(testcat), testcat.shape, test)
        tv_x = torch.FloatTensor(test).to(dev)
        #print(type(testcat), testcat.shape)
        #tv_y = testcat
        tv_y = torch.LongTensor(testcat).to(dev)
        y_hat = model(tv_x)
        #print("yhat", y_hat)
        #print(testcat)
        #print(type(y_hat), y_hat.shape)
        test_loss = model.bce(y_hat, tv_y)
        #print(len(tv_x),len(tv_y),len(y_hat),y_hat)
        (max_vals, arg_maxs) = torch.max(y_hat.data, dim=1)
        #print("max", max_vals.size(), max_vals);
        #print(arg_maxs.size(), arg_maxs)
        #print(tv_y.size(), tv_y)
        # arg_maxs is tensor of indices [0, 1, 0, 2, 1, 1 . . ]
        num_correct = torch.sum(tv_y==arg_maxs)
        acc = float(num_correct) / len(tv_y)
        #y_hat_class = np.where(y_hat.detach().numpy()<0.5, 0, 1)
        #accuracy = np.sum(tv_y.reshape(-1,1) == y_hat_class) / len(testcat)
        accuracy_score = acc
        train_accuracy_score = acc0

        print("Accs", acc0, acc)
        
        #y_hat_class = np.where(y_hat.detach().numpy()<0.5, 0, 1)
        #accuracy = np.sum(testcat.reshape(-1,1) == y_hat_class) / len(testcat)
        #accuracy_score = accuracy
        #train_loss.append(loss.item())

        #print("testlen", len(testcat))
        #print("test_loss")
        #print(test_loss)
        #print(accuracy_score)
        #print(type(accuracy_score))
        print("\nTest Accuracy: {0:f}\n".format(accuracy_score))
        return accuracy_score, test_loss, train_accuracy_score

    def getModel(self, myobj):
      if hasattr(myobj, 'modelInt'):  
        if myobj.modelInt == 1:
            modelname = 'mlp'
            config = myobj.pytorchMLPConfig
        if myobj.modelInt == 2:
            modelname = 'rnn'
            config = myobj.pytorchRNNConfig
        if myobj.modelInt == 3:
            modelname = 'lstm'
            config = myobj.pytorchLSTMConfig
        if myobj.modelInt == 4:
            modelname = 'gru'
            config = myobj.pytorchGRUConfig
        if myobj.modelInt == 5:
            modelname = 'cnn'
            config = myobj.pytorchCNNConfig
        if myobj.modelInt == 6:
            modelname = 'cnn2'
            config = myobj.pytorchCNN2Config
        if myobj.modelInt == 7:
            modelname = 'gptmidi'
            config = myobj.pytorchGPTMIDIConfig
        if myobj.modelInt == 8:
            modelname = 'gptmidirpr'
            config = myobj.pytorchGPTMIDIRPRConfig
        if myobj.modelInt == 9:
            modelname = 'gptmidifigaro'
            config = myobj.pytorchGPTMIDIFigaroConfig
        if myobj.modelInt == 10:
            modelname = 'gptmidimmt'
            config = myobj.pytorchGPTMIDIMMTConfig
        return config, modelname
      if hasattr(myobj, 'modelName'):
        if myobj.modelName == 'mlp':
          config = myobj.pytorchMLPConfig
        if myobj.modelName == 'cnn':
          config = myobj.pytorchCNNConfig
        if myobj.modelName == 'rnn':
          config = myobj.pytorchRNNConfig
        if myobj.modelName == 'lstm':
          config = myobj.pytorchLSTMConfig
        if myobj.modelName == 'gru':
          config = myobj.pytorchGRUConfig
        if myobj.modelName == 'cnn2':
          config = myobj.pytorchCNN2Config
        return config, myobj.modelName

    def wantDynamic(self, myobj):
        hasit = hasattr(myobj, 'neuralnetcommand')
        if not hasit or (hasit and myobj.neuralnetcommand.mldynamic):
            return True
        return False
    
    def wantLearn(self, myobj):
        hasit = hasattr(myobj, 'neuralnetcommand')
        if not hasit or (hasit and myobj.neuralnetcommand.mllearn):
            return True
        return False
    
    def wantClassify(self, myobj):
        hasit = hasattr(myobj, 'neuralnetcommand')
        if not hasit or (hasit and myobj.neuralnetcommand.mlclassify):
            return True
        return False

    def exists(self, myobj):
        if not hasattr(myobj, 'filename'):
            return False
        return os.path.isfile(self.getfullpath(myobj))

    #deprecated
    def existsds(self, myobj, modelname):
        if not hasattr(myobj, 'dataset'):
            return False
        return os.path.isfile(self.getdspath(myobj, modelname))

    def getdspath(self, myobj, modelname):
        if isinstance(myobj.dataset, list):
            dataset = str(myobj.dataset[0]) + str(myobj.dataset[1])
        else:
            dataset = myobj.dataset
        return self.getpath(myobj) + modelname + dataset + ".pt"

    def do_learntestclassify(self, queue, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        #myobj = json.loads(request, object_hook=lt.LearnTest)
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        classify = not hasattr(myobj, 'classify') or myobj.classify == True
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, size) = self.gettraintest(myobj, config, classify)
        myobj.size = size
        if not classify:
            if config.name == 'mlp':
                ii = 1
            else:
                #print("np", myobj.trainingarray, myobj.classifyarray)
                anarray = np.array(myobj.classifyarray, dtype='f')
                #print("np", anarray)
                myobj.classifyarray = anarray.reshape(anarray.shape[0], 1, anarray.shape[1])
        exists = self.exists(myobj)
        # load model if:
        # exists and not dynamic and wantclassify
        if exists and not self.wantDynamic(myobj) and self.wantClassify(myobj):
            print("Loading model")
            dev = self.getdev()
            checkpoint = torch.load(self.getfullpath(myobj), map_location = dev)
            model = checkpoint['model']
        else:
            model = Model.Net(myobj, config, classify)
        if torch.cuda.is_available():
            model.cuda()
        #model.share_memory()
        #myobj.size, myobj.classes, dim, layers)
        #testcat = torch.LongTensor(testcat)
        accuracy_score = None
        train_accuracy_score = None
        loss = None
        if self.wantLearn(myobj):
            (accuracy_score, loss, train_accuracy_score) = self.do_learntestinner(myobj, model, config, train, traincat, test, testcat, classify)
        # save model if
        # not dynamic and wantlearn
        if not self.wantDynamic(myobj) and self.wantLearn(myobj):
            torch.save({'model': model }, self.getfullpath(myobj))
        (intlist, problist) = (None, None)
        if self.wantClassify(myobj):
            (intlist, problist) = self.do_classifyinner(myobj, model, classify)
        #print(len(intlist))
        #print(intlist)
        #print(problist)
        if not accuracy_score is None:
            accuracy_score = float(accuracy_score)
        if not train_accuracy_score is None:
            train_accuracy_score = float(train_accuracy_score)
        if not loss is None:
            loss = float(loss)
        if not loss is None and np.isnan(loss):
            loss = None
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        return(Response(json.dumps({"classifycatarray": intlist, "classifyprobarray": problist, "accuracy": accuracy_score, "trainaccuracy": train_accuracy_score, "loss": loss, "gpu" : self.hasgpu()}), mimetype='application/json'))

    def do_dataset(self, queue, myjson):
        torch.cuda.empty_cache()
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(myjson)
        myobj = json.loads(myjson, object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, size, classes, classify) = mydatasets.getdataset(myobj, config, self)
        myobj.size = size
        myobj.classes = classes
        myobj.trainingarray = train
        myobj.trainingcatarray = traincat
        (train, traincat, test, testcat, size) = self.gettraintest(myobj, config, classify)
        model = Model.Net(myobj, config, classify)
        if torch.cuda.is_available():
            model.cuda()
            #cudnn.benchmark = True
        #model.share_memory()
        classifier = model
        print("model", modelname)
        (accuracy_score, loss, train_accuracy_score) = self.do_learntestinner(myobj, classifier, model, train, traincat, test, testcat, classify)
        myobj.classifyarray = train
        (intlist, problist) = self.do_classifyinner(myobj, model, classify)
        if not accuracy_score is None:
            accuracy_score = float(accuracy_score)
        if not train_accuracy_score is None:
            train_accuracy_score = float(train_accuracy_score)
        if not loss is None:
            loss = float(loss)
        if not loss is None and np.isnan(loss):
            loss = None
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        return({"accuracy": accuracy_score, "trainaccuracy": train_accuracy_score, "loss": loss, "classify" : classify, "gpu" : self.hasgpu() })

    def do_dataset_gen(self, queue, request):
        import discriminator
        import generator
        import model.gan
        torch.cuda.empty_cache()
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        config = myobj
        #(config, modelname) = self.getModel(myobj)
        #Model = importlib.import_module('model.' + modelname)

        size, classes, dl = mydatasets.getmnistdl(None)

        adiscriminator = discriminator.Discriminator(in_features=size, out_features=classes)
        agenerator = generator.Generator(in_features=100, out_features=size)
        #agenerator = Generator(100, 32, 784)
        print(adiscriminator)
        print()
        print(agenerator)

        discriminator_optim = torch.optim.Adam(adiscriminator.parameters(), lr=config.lr)
        generator_optim = torch.optim.Adam(agenerator.parameters(), lr=config.lr)

        loss_fn = torch.nn.BCEWithLogitsLoss()

        device = self.getdev()

        #n_epochs = 10
        discriminator_losses, generator_losses = model.gan.train_mnist_gan(adiscriminator, agenerator, discriminator_optim, generator_optim,
                                                                           loss_fn, dl, config.epochs, device,
                                                                           verbose=False)

        print("Losses", discriminator_losses, generator_losses)
        
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)

        return (Response(json.dumps(
                {"accuracy": 0, "trainaccuracy": 0, "loss": discriminator_losses + generator_losses, "classify": "",
                 "gpu": self.hasgpu()}), mimetype='application/json'))

    def do_gpt(self, queue, myjson, cachedata):
        dt = datetime.now()
        timestamp = dt.timestamp()
        myobj = json.loads(myjson, object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        if cachedata is not None:
            print("Using cache")
            model = cachedata
            model.myobj = myobj
        else:
            datasets = mydatasets.getdatasettext(myobj, config, self)
            model = Model.Model(myobj, config, datasets)
        exists = self.exists(myobj)
        print("exist", exists)
        # load model if:
        # exists and not dynamic and wantclassify
        text = None
        if exists and not self.wantDynamic(myobj) and self.wantClassify(myobj):
            if model.localsave():
                if cachedata is None:
                    # dummy variable to allow saver
                    model = Model.Model(myobj, config, datasets)
                    print("Restoring")
                    dev = self.getdev()
                    checkpoint = torch.load(self.getfullpath(myobj), map_location=dev)
                    model = checkpoint['model']
                    print("Restoring done")
                text = model.generate(model.model)
                print("text", text)
            else:
                model = Model.Model(myobj, config, datasets)
        else:
             model = Model.Model(myobj, config, datasets)
        # load end
        # print("classez2", myobj.classes)
        print(model)
        self.printgpus()
        classifier = model
        # dictclass[str(myobj.modelInt) + myobj.period + myobj.modelname] = classifier
        # global dicteval
        # dicteval[myobj.modelname] = float(accuracy_score)
        # print("seteval" + str(myobj.modelname))

        if not self.wantDynamic(myobj) and self.wantLearn(myobj):
            print(model.model.summary())
            if model.dataset.train_ds is not None:
                model.fit()
            if model.localsave():
                print("Saving")
                torch.save({'model': model }, self.getfullpath(myobj))

        #classifier.tidy()
        del classifier
        accuracy_score = 0
        train_accuracy_score = 0
        loss = 0
        if not accuracy_score is None:
            accuracy_score = float(accuracy_score)
        if not train_accuracy_score is None:
            train_accuracy_score = float(train_accuracy_score)
        if not loss is None:
            loss = float(loss)
        dt = datetime.now()
        #datadict = { "model" : model, "datasets" : datasets, "md" : md }
        #newdata2 = json.dumps(datadict)
        queue.put({"accuracy": accuracy_score, "trainaccuracy": train_accuracy_score, "loss": loss, "classify": None, 'classifyarray' : [ text ],
             "gpu": self.hasgpu() })
        return model

    def do_gptmidi(self, queue, myjson, filenames, cachedata):
        dt = datetime.now()
        timestamp = dt.timestamp()
        filename = filenames[0]
        myobj = json.loads(myjson, object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        if cachedata is not None:
            print("Using cache")
            model = cachedata
            model.myobj = myobj
        else:
            if not hasattr(myobj, 'flavour'):
                datasets = mydatasets.getdatasetmidi(myobj, config, self)
            else:
                import mydatasetsl
                datamodule = mydatasetsl.getdatasetmidi(myobj, config, self)
                datasets = datamodule
            model = Model.Model(myobj, config, datasets)
        exists = self.exists(myobj)
        print("exist", exists)
        # load model if:
        # exists and not dynamic and wantclassify
        files = None
        if exists and not self.wantDynamic(myobj) and self.wantClassify(myobj):
            if model.localsave():
                if cachedata is None:
                    # dummy variable to allow saver
                    model = Model.Model(myobj, config, datasets)
                    print("Restoring")
                    dev = self.getdev()
                    #checkpoint = torch.load(self.getfullpath(myobj), map_location=dev)
                    #model = checkpoint['model']
                    if not hasattr(myobj, 'flavour'):
                        model.model.load_state_dict(torch.load(self.getfullpath(myobj)))
                    else:
                        model = model_class.load_from_checkpoint(checkpoint_path=CHECKPOINT)
                    print("Restoring done")
                    print("training", model.model.training)
                    if hasattr(model.model, 'test'):
                        model.model.test()
                    print("training", model.model.training)
                files = model.generate(filename)
                print("files", files)
            else:
                model = Model.Model(myobj, config, datasets)
        else:
             model = Model.Model(myobj, config, datasets)
        # load end
        # print("classez2", myobj.classes)
        print(model)
        #self.printgpus()
        classifier = model
        # dictclass[str(myobj.modelInt) + myobj.period + myobj.modelname] = classifier
        # global dicteval
        # dicteval[myobj.modelname] = float(accuracy_score)
        # print("seteval" + str(myobj.modelname))

        if not self.wantDynamic(myobj) and self.wantLearn(myobj):
            #print(model.model.summary())
            print(model.dataset)
            if model.dataset.train_loader is not None:
                model.fit()
            if model.localsave():
                print("Saving")
                #torch.save({'model': model }, self.getfullpath(myobj))
                if not hasattr(myobj, 'flavour'):
                    torch.save(model.model.state_dict(), self.getfullpath(myobj))
                else:
                    model.save()

        #classifier.tidy()
        del classifier
        accuracy_score = 0
        train_accuracy_score = 0
        loss = 0
        if not accuracy_score is None:
            accuracy_score = float(accuracy_score)
        if not train_accuracy_score is None:
            train_accuracy_score = float(train_accuracy_score)
        if not loss is None:
            loss = float(loss)
        dt = datetime.now()
        #datadict = { "model" : model, "datasets" : datasets, "md" : md }
        #newdata2 = json.dumps(datadict)
        queue.put({"accuracy": accuracy_score, "trainaccuracy": train_accuracy_score, "loss": loss, "classify": None,
             "gpu": self.hasgpu(), "files" : files })
        return model

    def get_file(self, request):
        filename = self.getFilename(request, 'file')
        filename2 = self.getFilename(request, 'file2')
        return (filename, filename2)

    def getFilename(self, request, key):
        if key not in request.files:
            print('No file part')
            # print(request.files)
            return None
        file = request.files[key]
        if file.filename == '':
            print('No selected file')
            return None
        if file:
            filename = secure_filename(file.filename)
            file.save("/tmp/" + filename)
            filename = "/tmp/" + filename
        return filename

    def getpath(self, myobj):
        if hasattr(myobj, 'path') and not myobj.path is None:
            return myobj.path + '/'
        return '/tmp/'

    def getfullpath(self, myobj):
        return self.getpath(myobj) + myobj.filename + ".pt"

    def do_filename(self, queue, request):
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        exists = self.exists(myobj)
        return(Response(json.dumps({"exists": exists}), mimetype='application/json'))

    def hasgpu(self):
        return torch.cuda.is_available()
    
    def getpu(self):
        if torch.cuda.is_available():  
            return "cuda:0" 
        else:  
            return "cpu"  

    def getdev(self):
        return torch.device(self.getpu())

    def hasgpu(self):
        return torch.cuda.is_available()
