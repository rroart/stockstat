import learntest as lt
import os

import pandas as pd
import tensorflow as tf
import keras
import numpy as np

import importlib

import json
from datetime import datetime
from werkzeug.wrappers import Response
from werkzeug.utils import secure_filename
import inspect
import shutil

from multiprocessing import Queue

import mydatasets
from mydatasets import DictToObject

global dicteval
dicteval = {}
global dictclass
dictclass = {}
global count
count = 0

try:
    import device
    pu = device.get_pu()
    print("Using pu ", pu)
    print("Keras", keras.__version__)
    print("Tensorflow Keras", tf.keras.__version__)
except:
    import sys,traceback
    traceback.print_exc(file=sys.stdout)
    
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
        (intlist, problist) = self.do_classifyinner(myobj, classifier)
        #print(len(intlist))
        #print(intlist)
        #print(problist)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        
        return Response(json.dumps({"classifycatarray": intlist, "classifyprobarray": problist }), mimetype='application/json')
        
    def do_classifyinner(self, myobj, classifier, classify):
        array = np.array(myobj.classifyarray, dtype='f')
        dataset = tf.data.Dataset.from_tensor_slices((array))
        intlist = []
        problist = []
        #print("l",len(array))
        if not classify:
            if myobj.modelInt == 3 or myobj.modelInt == 8:
                predicted = np.empty((len(array), 0))
            else:
                predicted = np.empty((len(array), 1, 0))
            #print(predicted)
            #print(myobj.size, myobj.classes)
            #print("pred", predicted.shape, predicted)
            for i in range(myobj.classes):
                #print("i", i)
                #print("ashape", array.shape)
                print("ar", array)
                (intlist, problist) = classifier.predict(array)
                intlist = np.array(intlist)
                print("pr", intlist)
                if myobj.modelInt == 3 or myobj.modelInt == 8:
                    intlist2 = intlist.reshape(len(intlist), 1)
                    array = np.concatenate((array[:,1:], intlist2), axis=1)
                    #print(predicted.shape)
                    #print(intlist2.shape)
                    predicted = np.concatenate((predicted, intlist2), axis=1)
                else:
                    intlist3 = intlist.reshape(len(intlist), 1, 1)
                    array = np.concatenate((array[:,:,1:], intlist3), axis=2)
                    predicted = np.concatenate((predicted, intlist3), axis=2)
            if not myobj.modelInt == 3 and not myobj.modelInt == 8:
                predicted = predicted.reshape(predicted.shape[0], predicted.shape[2])
            #print("predarray", predicted.shape, predicted)
            predicted = predicted.tolist()
            return predicted, problist
        else:
            print("Classify", array.shape)
            (intlist, problist) = classifier.predict(array)
        if classify and not self.zero(myobj):
            intlist = np.array(intlist)
            intlist = intlist + 1
            intlist = intlist.tolist()
        return intlist, problist

    def do_learntest(self, queue, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        classify = not hasattr(myobj, 'classify') or myobj.classify == True
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, size) = self.gettraintest(myobj, config, classify)
        myobj.size = size
        model = Model.Model(myobj, config, classify)
        classifier = model
        (accuracy_score, loss, train_accuracy_score) = self.do_learntestinner(myobj, classifier, train, traincat, test, testcat, classify)
        global dictclass
        #dictclass[str(myobj.modelInt) + myobj.period + myobj.modelname] = classifier
        #global dicteval
        #dicteval[myobj.modelname] = float(accuracy_score)
        #print("seteval" + str(myobj.modelname))
        
        classifier.tidy()
        del classifier
        dt = datetime.now()
        #print ("millis ", (dt.timestamp() - timestamp)*1000)
        queue.put(Response(json.dumps({"accuracy": float(accuracy_score), "trainaccuracy": float(train_accuracy_score)}), mimetype='application/json'))
        #return Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json')

    def getSlide(self, inputs, labels, myobj, config):
        #input = torch.from_numpy(np.array([[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20]]]))
        #inputs = torch.from_numpy(inputs)
        inputs = np.array(inputs, dtype='f')
        size = inputs.shape
        if  hasattr(config, 'slide_stride'):
            sliding_window_stride = config.slide_stride
        else:
            sliding_window_stride = 1
        sliding_window_width = myobj.size
        #print(type(size))
        print(size)
        mysize = size[0]
        sequence_length = size[1]
        arange = int((sequence_length - (sliding_window_width) - myobj.classes) / sliding_window_stride) + 1
        print("arange", arange)
        splitInput = np.zeros((mysize * arange, sliding_window_width))
        splitTarget = np.zeros((mysize * arange))
        #print(inputs.shape)
        #print(splitInput.shape)
        #print(splitTarget.shape)
        # [seq_length, batch_size, features],
        for i in range(mysize):
            for j in range(arange):
                splitInput[i * arange + j] = inputs[i, j * sliding_window_stride : (j * sliding_window_stride + sliding_window_width)]
                splitTarget[i * arange + j] = inputs[i, j * sliding_window_stride + sliding_window_width]
        inputs = splitInput
        labels = splitTarget
        #print("hh")
        #print(inputs)
        #print(labels)
        #myobj.classes = 1
        if config.name == 'mlp' or config.name == 'lir':
            ii = 1
        else:
            #print("notmlp")
            #print(splitInput.shape)
            myobj.size = (1, sliding_window_width)
            inputs = inputs.reshape(mysize * arange, 1, sliding_window_width)
            #print(splitInput.shape)
        return inputs, labels

    def getSlide2(self, inputs, labels, myobj, config):
        #input = torch.from_numpy(np.array([[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20]]]))
        #inputs = torch.from_numpy(inputs)
        inputs = np.array(inputs, dtype='f')
        size = inputs.shape
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
        splitInput = np.zeros((arange * mysize, minibatch, sliding_window_width))
        splitTarget = np.zeros((arange * mysize, minibatch, myobj.classes))
        #print(inputs.shape)
        #print(splitInput.shape)
        #print(splitTarget.shape)
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
        #print("hh")
        #print(inputs)
        #print(labels)
        return inputs, labels

    def getSlideT(self, inputs, labels, myobj, config):
        #input = torch.from_numpy(np.array([[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20]]]))
        #inputs = torch.from_numpy(inputs)
        inputs = tf.convert_to_tensor(inputs, dtype=tf.float32)
        size = inputs.shape
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
        splitInput = tf.Variable(tf.zeros((arange * mysize, minibatch, sliding_window_width)))
        splitTarget = tf.Variable(tf.zeros((arange * mysize, minibatch, myobj.classes)))
        #print(inputs.shape)
        #print(splitInput.shape)
        #print(splitTarget.shape)
        # [seq_length, batch_size, features],
        for i in range(mysize):
            for j in range(minibatch):
                for k in range(arange):
                    #print(i, j*arange + k, j, k*sliding_window_stride, k*sliding_window_stride+sliding_window_width)
                    #print(splitInput[j * arange + i].size())
                    #print(inputs[j, :, i * sliding_window_stride : (i * sliding_window_stride + sliding_window_width)].size())
                    #print(splitTarget[i, j * arange].size())
                    #print(labels[j, :, :].size())
                    splitInput = splitInput[i * arange + k, j].assign(inputs[i, j, k * sliding_window_stride : (k * sliding_window_stride + sliding_window_width)])
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

    # non-zero is default
    def zero(self, myobj):
        return hasattr(myobj, 'zero') and myobj.zero == True

    def gettraintest(self, myobj, config, classify):
        mydim = myobj.size
        array = np.array(myobj.trainingarray, dtype='f')
        if config.name == "cnn2":
            print("cnn2 shape")
            print(array.shape)
            #array = np.transpose(array, [0, 3, 2, 1])
            array = np.transpose(array, [0, 2, 3, 1])
            print(array.shape)

        cat = np.array([], dtype='i')
        if hasattr(myobj, 'trainingcatarray') and not myobj.trainingcatarray is None:
            cat = np.array(myobj.trainingcatarray, dtype='i')
        #print(array)
        # NOTE class range 1 - 4 will be changed to 0 - 3
        # to avoid risk of being classified as 0 later
        if not self.zero(myobj):
            cat = cat - 1
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
            train = array[:half, :]
            test = array[half:, :]
            traincat = cat[:half]
            testcat = cat[half:]
            if len(cat) == 1:
                train = array
                test = array
                traincat = cat
                testcat = cat
        #print("classes")
        #print(myobj.classes)
        #print("cwd")
        #print(os.getcwd())
        #print(classifier)
        #print("train", train);
        #print(traincat)
        #print("mydim");
        #print(mydim)
        if len(train.shape) == 2:
            mydim = train.shape[1]
        else:
            mydim = train.shape[1:]
        #print(mydim)
        print("Shapes ", train.shape, traincat.shape, mydim)
        return train, traincat, test, testcat, mydim
    
    def do_learntestinner(self, myobj, classifier, train, traincat, test, testcat, classify):
        print("ttt", train)
        print("ttt2", traincat)
        print("ttt3", test)
        print("ttt4", testcat)
        #print("shape")
        #print(train.shape)
        #print(traincat.shape)
        #print(classifier.train)
        classifier.train(train, traincat)
        #print(train)
        #print(traincat)
        (intlist, problist) = classifier.predict(test)
        #print("tests", myobj.modelInt)
        #print(testcat)
        #print(intlist)
        #print(problist)

        if not classify:
            tv_x = test
            y_hat = (tv_x)
            #loss = model.bce(y_hat, testcat)
            #tv_y = torch.FloatTensor(testcat)
            #print("losss", loss.item())
            #print(tv_y.size(), tv_y)
            #print(y_hat.size(),y_hat)
            #return None, loss.item()

        train_loss, train_accuracy_score = classifier.evaluate(train, traincat)
        
        test_loss, accuracy_score = classifier.evaluate(test, testcat)

        if isinstance(classifier, tf.keras.Model):
            print(classifier.metrics_names)
            print(classifier.summary())
        print("Accs", train_accuracy_score, accuracy_score)
        #print("test_loss")
        #print(test_loss)
        #print(accuracy_score)
        #print(type(accuracy_score))
        #print("\nTest Accuracy: {0:f}\n".format(accuracy_score))
        return accuracy_score, test_loss, train_accuracy_score

    def getModel(self, myobj):
      print(tf.__version__)
      if hasattr(myobj, 'modelInt'):
        if myobj.modelInt == 1:
            modelname = 'dnn'
            config = myobj.tensorflowDNNConfig
        if myobj.modelInt == 2:
            modelname = 'lic'
            config = myobj.tensorflowLICConfig
        if myobj.modelInt == 3:
            modelname = 'mlp'
            config = myobj.tensorflowMLPConfig
        if myobj.modelInt == 4:
            modelname = 'rnn'
            config = myobj.tensorflowRNNConfig
        if myobj.modelInt == 5:
            modelname = 'cnn'
            config = myobj.tensorflowCNNConfig
        if myobj.modelInt == 6:
            modelname = 'lstm'
            config = myobj.tensorflowLSTMConfig
        if myobj.modelInt == 7:
            modelname = 'gru'
            config = myobj.tensorflowGRUConfig
        if myobj.modelInt == 8:
            modelname = 'lir'
            config = myobj.tensorflowLIRConfig
        if myobj.modelInt == 9:
            modelname = 'cnn2'
            config = myobj.tensorflowCNN2Config
        if myobj.modelInt == 10:
            modelname = 'qnn'
            config = myobj.tensorflowQNNConfig
        if myobj.modelInt == 11:
            modelname = 'qcnn'
            config = myobj.tensorflowQCNNConfig
        if myobj.modelInt == 12:
            modelname = 'conditional_gan'
            config = myobj.tensorflowConditionalGANConfig
        if myobj.modelInt == 13:
            modelname = 'dcgan'
            config = myobj.tensorflowDCGANConfig
        if myobj.modelInt == 14:
            modelname = 'neural_style_transfer'
            config = myobj.tensorflowNeuralStyleTransferConfig
        if myobj.modelInt == 15:
            modelname = 'miniature_gpt'
            config = myobj.tensorflowMiniatureGPTConfig
        if myobj.modelInt == 16:
            modelname = 'gpt'
            config = myobj.tensorflowGPTConfig
        if myobj.modelInt == 17:
            modelname = 'gpt2'
            config = myobj.tensorflowGPT2Config
        return config, modelname
      if hasattr(myobj, 'modelName'):
        if myobj.modelName == 'dnn':
            config = myobj.tensorflowDNNConfig
        if myobj.modelName == 'lic':
            config = myobj.tensorflowLICConfig
        if myobj.modelName == 'lir':
            config = myobj.tensorflowLIRConfig
        if myobj.modelName == 'mlp':
            config = myobj.tensorflowMLPConfig
        if myobj.modelName == 'rnn':
            config = myobj.tensorflowRNNConfig
        if myobj.modelName == 'lstm':
            config = myobj.tensorflowLSTMConfig
        if myobj.modelName == 'gru':
            config = myobj.tensorflowGRUConfig
        if myobj.modelName == 'cnn':
            config = myobj.tensorflowCNNConfig
        if myobj.modelName == 'cnn2':
            config = myobj.tensorflowCNN2Config
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
        if os.path.exists(self.getpath(myobj) + myobj.filename):
            return True
        return os.path.isfile(self.getpath(myobj) + myobj.filename + ".keras")

    def existsds(self, myobj):
        if not hasattr(myobj, 'dataset'):
            return False
        return os.path.isfile(self.getpath(myobj) + myobj.dataset + ".keras")

    def do_learntestclassify(self, queue, request):
        print("eager", tf.executing_eagerly())
        #tf.logging.set_verbosity(tf.logging.FATAL)
        dt = datetime.now()
        timestamp = dt.timestamp()
        print(request.get_data(as_text=True))
        #myobj = json.loads(request, object_hook=lt.LearnTest)
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        classify = not hasattr(myobj, 'classify') or myobj.classify == True
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, size) = self.gettraintest(myobj, config, classify)
        myobj.size = size
        if not classify:
            if config.name == 'mlp'or config.name == 'lir':
                ii = 1
            else:
                anarray = np.array(myobj.classifyarray, dtype='f')
                myobj.classifyarray = anarray.reshape(anarray.shape[0], 1, anarray.shape[1])
        exists = self.exists(myobj)
        # load model if:                                                               # exists and not dynamic and wantclassify
        if exists and not self.wantDynamic(myobj) and self.wantClassify(myobj):
            if Model.Model.localsave():
                # dummy variable to allow saver
                model = Model.Model(myobj, config, classify)
                print("Restoring")
                model.model = tf.keras.models.load_model( self.getpath(myobj) + myobj.filename + ".keras")
                print("Restoring done")
            else:
                model = Model.Model(myobj, config, classify)
        else:
            model = Model.Model(myobj, config, classify)
        classifier = model
        if config.name == "rnnnot":
            train = np.transpose(train, [1, 0, 2])
            test = np.transpose(test, [1, 0, 2])
            
        accuracy_score = None
        train_accuracy_score = None
        loss = None
        if self.wantLearn(myobj):
            (accuracy_score, loss, train_accuracy_score) = self.do_learntestinner(myobj, classifier, train, traincat, test, testcat, classify)
        #print(type(classifier))

        #print("neuralnetcommand")
        #print(myobj.neuralnetcommand.mlclassify, myobj.neuralnetcommand.mllearn, myobj.neuralnetcommand.mldynamic)
        # save model if                                                                # not dynamic and wantlearn
        if not self.wantDynamic(myobj) and self.wantLearn(myobj):
            if Model.Model.localsave():
                print("Saving")
                model.save(self.getpath(myobj) + myobj.filename + ".keras")

        (intlist, problist) = (None, None)
        if self.wantClassify(myobj):
            #print("here0");
            if config.name == "cnn2":
                print("cnn2 shape")
                array = np.array(myobj.classifyarray, dtype='f')
                print(array.shape)
                #array = np.transpose(array, [0, 3, 2, 1])
                array = np.transpose(array, [0, 2, 3, 1])
                print(array.shape)
                myobj.classifyarray = array
            (intlist, problist) = self.do_classifyinner(myobj, classifier, classify)
        #print("here00");
        #print(len(intlist))
        #print(intlist)
        #print(problist)
        classifier.tidy()
        del classifier
        if not accuracy_score is None:
            accuracy_score = float(accuracy_score) 
        if not train_accuracy_score is None:
            train_accuracy_score = float(train_accuracy_score)
        if not loss is None:
            loss = float(loss)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        queue.put(Response(json.dumps({"classifycatarray": intlist, "classifyprobarray": problist, "accuracy": accuracy_score, "trainaccuracy": train_accuracy_score, "loss": loss, "gpu" : self.hasgpu() }), mimetype='application/json'))

    def do_dataset(self, queue, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, origsize, size, classes, classify) = mydatasets.getdataset(myobj, config, self)
        train = np.array(train)
        myobj.trainingarray = train
        myobj.trainingcatarray = traincat
        myobj.size = size
        myobj.classes = classes
        (train, traincat, test, testcat, size) = self.gettraintest(myobj, config, classify)
        #print("classez2", myobj.classes)
        model = Model.Model(myobj, config, classify)
        exists = self.exists(myobj)
        # load model if:
        # exists and not dynamic and wantclassify
        if exists and not self.wantDynamic(myobj) and self.wantClassify(myobj):
            if Model.Model.localsave():
                # dummy variable to allow saver
                model = Model.Model(myobj, config, classify)
                print("Restoring")
                model.model = tf.keras.models.load_model( self.getpath(myobj) + myobj.filename + ".keras")
                print("Restoring done")
            else:
                model = Model.Model(myobj, config, classify)
        else:
            model = Model.Model(myobj, config, classify)
        # load end
        #print("classez2", myobj.classes)
        print(model)
        self.printgpus()
        classifier = model
        (accuracy_score, loss, train_accuracy_score) = self.do_learntestinner(myobj, classifier, train, traincat, test, testcat, classify)
        myobj.classifyarray = train
        (intlist, problist) = self.do_classifyinner(myobj, model, classify)
        global dictclass
        #dictclass[str(myobj.modelInt) + myobj.period + myobj.modelname] = classifier
        #global dicteval
        #dicteval[myobj.modelname] = float(accuracy_score)
        #print("seteval" + str(myobj.modelname))
        
        if not self.wantDynamic(myobj) and self.wantLearn(myobj):
            if Model.Model.localsave():
                print("Saving")
                model.save(self.getpath(myobj) + myobj.filename + ".keras")

        classifier.tidy()
        del classifier
        if not accuracy_score is None:
            accuracy_score = float(accuracy_score)
        if not train_accuracy_score is None:
            train_accuracy_score = float(train_accuracy_score)
        if not loss is None:
            loss = float(loss)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        queue.put(Response(json.dumps({"accuracy": accuracy_score, "trainaccuracy": train_accuracy_score, "loss": loss, "classify" : classify, "gpu" : self.hasgpu() }), mimetype='application/json'))
        #return Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json')

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

    def do_dataset_gen(self, queue, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print("1111")
        #print(request.get_data(as_text=True))
        #print("2222")

        (filename, filename2) = self.get_file(request)
        print("Filename", filename, filename2)

        #print("rrr0", request.form)
        #print("rrr", request.files['json'])
        myobj = json.loads(request.form['json'], object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        if not modelname == 'neural_style_transfer':
            (dataset, size, classes, classify, from_logits) = mydatasets.getdataset2(myobj, config, self)

            myobj.size = size
            myobj.classes = classes

            model = Model.Model(myobj, config)
        else:
            model = Model.Model(myobj, config, filename, filename2)

        exists = self.exists(myobj)
        # load model if:
        # exists and not dynamic and wantclassify
        #if exists and not self.wantDynamic(myobj) and self.wantClassify(myobj):
        #    if Model.Model.localsave():
        #        # dummy variable to allow saver
        #        model = Model.Model(myobj, config, classify)
        #        print("Restoring")
        #        model.model = tf.keras.models.load_model( self.getpath(myobj) + myobj.filename + ".keras")
        #        print("Restoring done")
        #    else:
        #        model = Model.Model(myobj, config, classify)
        #else:
        #    model = Model.Model(myobj, config, classify)
        # load end

        # print("classez2", myobj.classes)
        print(model)
        self.printgpus()

        """
        ## Training the Conditional GAN
        """
        
        #cond_gan = ConditionalGAN(
        #    discriminator=discriminator, generator=generator, latent_dim=latent_dim
        #)
        if not modelname == 'neural_style_transfer':
            gan = model
            gan.compile(
                d_optimizer=tf.keras.optimizers.Adam(learning_rate=config.lr),
                g_optimizer=tf.keras.optimizers.Adam(learning_rate=config.lr),
                loss_fn=tf.keras.losses.BinaryCrossentropy(from_logits=from_logits),
            )

            gan.fit(dataset, epochs=config.steps, callbacks = [ gan.getcallback() ])
        else:
            gan = model

        #if not self.wantDynamic(myobj) and self.wantLearn(myobj):
        #    if model.localsave():
        #        print("Saving")
        #        model.save(self.getpath(myobj) + myobj.filename + ".keras")

        if hasattr(myobj, 'generate') and myobj.generate:
            files = gan.generate()
            print("files", files)

        classifier = model

        #classifier.tidy()
        del classifier
        dt = datetime.now()
        print("millis ", (dt.timestamp() - timestamp) * 1000)
        queue.put(Response(json.dumps(
            {"accuracy": 0, "trainaccuracy": 0, "loss": 0, "classify": False,
             "gpu": self.hasgpu(), "files" : files } ), mimetype='application/json'))
        # return Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json')

    def do_imgclassify(self, queue, request):
        print("eager", tf.executing_eagerly())
        #tf.logging.set_verbosity(tf.logging.FATAL)
        dt = datetime.now()
        timestamp = dt.timestamp()
        #myobj = json.loads(request, object_hook=lt.LearnTest)
        (filename, filename2) = self.get_file(request)
        print("Filename", filename, filename2)
        myobj = json.loads(request.form['json'], object_hook=lt.LearnTest)

        (config, modelname) = self.getModel(myobj)
        print("Cf", config, modelname)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, origsize, size, classes, classify) = mydatasets.getdataset(myobj, config, self)

        img = self.preprocess_image(filename, origsize)

        train = np.array(train)
        myobj.trainingarray = train
        myobj.trainingcatarray = traincat
        myobj.size = size
        myobj.classes = classes
        (train, traincat, test, testcat, size) = self.gettraintest(myobj, config, classify)
        # print("classez2", myobj.classes)
        model = Model.Model(myobj, config, classify)
        exists = self.exists(myobj)
        # load model if:
        # exists and not dynamic and wantclassify
        if exists and not self.wantDynamic(myobj) and self.wantClassify(myobj):
            if Model.Model.localsave():
                # dummy variable to allow saver
                model = Model.Model(myobj, config, classify)
                print("Restoring")
                model.model = tf.keras.models.load_model(self.getpath(myobj) + myobj.filename + ".keras")
                print("Restoring done")
            else:
                model = Model.Model(myobj, config, classify)
        else:
            model = Model.Model(myobj, config, classify)
        # load end
        # print("classez2", myobj.classes)
        print(model)
        self.printgpus()
        classifier = model
        #print("rrr0", request.form)
        #print("rrr", request.files['json'])
        myobj = json.loads(request.form['json'], object_hook=lt.LearnTest)
        if config.name == 'mlp':
            print(type(img))
            print(img.shape)
            img = tf.reshape(img, (img.shape[0], img.shape[1] * img.shape[2], img.shape[3]))
            print(type(img))
            print(img.shape)
        myobj.classifyarray = img
        (intlist, problist) = self.do_classifyinner(myobj, model, True)
        print("list", intlist, problist)
        classifier.tidy()
        del classifier
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        queue.put(Response(json.dumps({"classifycatarray": intlist, "classifyprobarray": problist, "accuracy": 0, "trainaccuracy": 0, "loss": 0, "classify" : classify, "gpu" : self.hasgpu() }), mimetype='application/json'))

    def do_gpt(self, queue, request, cachedata):
        dt = datetime.now()
        timestamp = dt.timestamp()
        # print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        if cachedata is not None:
            print("Using cache")
            datasets = cachedata.datasets
            md = cachedata.md
            model = cachedata.model
        else:
            (datasets, md) = mydatasets.getdataset3(myobj, config, self)
            model = Model.Model(myobj, config, md)
        exists = self.existsds(myobj)
        print("exist", exists)
        # load model if:
        # exists and not dynamic and wantclassify
        text = None
        if cachedata is None:
            if exists and not self.wantDynamic(myobj) and self.wantClassify(myobj):
                if model.localsave():
                    # dummy variable to allow saver
                    model = Model.Model(myobj, config, md)
                    print("Restoring")
                    model.model = tf.keras.models.load_model(self.getpath(myobj) + myobj.dataset + ".keras")
                    print("Restoring done")
                    text = model.generate(model.model);
                    print("text", text)
                else:
                    model = Model.Model(myobj, config, md)
            else:
                model = Model.Model(myobj, config, md)
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
            if datasets.train_ds is not None:
                model.fit(datasets.train_ds, datasets.val_ds, datasets.test_ds)
            if model.localsave():
                print("Saving")
                model.save(self.getpath(myobj) + myobj.dataset + ".keras")

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
        # return Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json')

    def preprocess_image(self, image_path, size):
        import keras
        #import cv2
        from keras.applications import vgg19
        #TODO
        img_nrows = size[1]
        width, height = keras.utils.load_img(image_path).size
        print("real size", width, height)
        img_ncols = int(width * img_nrows / height)
        img_ncols = size[0]
        print("new size", img_nrows, img_ncols)
        # duplicated
        # Util function to open, resize and format pictures into appropriate tensors
        img = keras.utils.load_img(image_path, target_size=(img_nrows, img_ncols))
        print("sh", img.size)
        img = keras.utils.img_to_array(img)
        print("sh", img.size, img.shape)
        img = np.expand_dims(img, axis=0)
        print("sh", img.size, img.shape)
        #zimg = vgg19.preprocess_input(img)
        img = tf.image.rgb_to_grayscale(img)
        #print("img", img)
        print("sh", img.shape)
        return tf.convert_to_tensor(img)

    def getpath(self, myobj):
        if hasattr(myobj, 'path') and not myobj.path is None:
            return myobj.path + '/'
        return '/tmp/'

    def do_filename(self, queue, request):
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        exists = self.exists(myobj)
        queue.put(Response(json.dumps({"exists": exists}), mimetype='application/json'))

    def printgpus(self):
        #from tensorflow.python.client import device_lib
        #print(device_lib.list_local_devices())
        from keras import backend as K
        gpus = tf.config.list_physical_devices('GPU') 
        print(type(gpus))
        print("GPUs", gpus)

    def hasgpu(self):
        physical_devices = tf.config.list_physical_devices('GPU')
        return len(physical_devices) > 0
