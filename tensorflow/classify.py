import learntest as lt
import device
import os

import pandas as pd
import tensorflow as tf
import numpy as np

import importlib

import json
from datetime import datetime
from werkzeug.wrappers import Response
import shutil

from multiprocessing import Queue

import mydatasets

global dicteval
dicteval = {}
global dictclass
dictclass = {}
global count
count = 0

pu = device.get_pu()
print("Using pu ", pu)

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
        global dictclass
        classifier = dictclass[myobj.modelname]
        del dictclass[myobj.modelname]
        (intlist, problist) = self.do_classifyinner(myobj, classifier)
        print(len(intlist))
        print(intlist)
        print(problist)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        
        return Response(json.dumps({"classifycatarray": intlist, "classifyprobarray": problist }), mimetype='application/json')
        
    def do_classifyinner(self, myobj, classifier):
        array = np.array(myobj.classifyarray, dtype='f')
        dataset = tf.data.Dataset.from_tensor_slices((array))
        intlist = []
        problist = []
        (intlist, problist) = classifier.predict(array)
        if not self.zero(myobj):
            intlist = np.array(intlist)
            intlist = intlist + 1
            intlist = intlist.tolist()
        return intlist, problist

    def do_learntest(self, queue, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, size) = self.gettraintest(myobj, config)
        myobj.size = size
        model = Model.Model(myobj, config)
        classifier = model
        accuracy_score = self.do_learntestinner(myobj, classifier, train, traincat, test, testcat)
        global dictclass
        #dictclass[str(myobj.modelInt) + myobj.period + myobj.modelname] = classifier
        #global dicteval
        #dicteval[myobj.modelname] = float(accuracy_score)
        #print("seteval" + str(myobj.modelname))
        
        classifier.tidy()
        del classifier
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        queue.put(Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json'))
        #return Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json')

    # non-zero is default
    def zero(self, myobj):
        return hasattr(myobj, 'zero') and myobj.zero == True

    def gettraintest(self, myobj, config):
        mydim = myobj.size
        array = np.array(myobj.trainingarray, dtype='f')
        cat = np.array(myobj.trainingcatarray, dtype='i')
        #print(array)
        # NOTE class range 1 - 4 will be changed to 0 - 3
        # to avoid risk of being classified as 0 later
        if not self.zero(myobj):
            cat = cat - 1
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
        print("mydim");
        print(mydim)
        if len(train.shape) == 2:
            mydim = train.shape[1]
        else:
            mydim = train.shape[1:]
        print(mydim)
        return train, traincat, test, testcat, mydim
    
    def do_learntestinner(self, myobj, classifier, train, traincat, test, testcat):
        print("shape")
        print(train.shape)
        classifier.train(train, traincat)
        #print(train)
        #print(traincat)
        (intlist, problist) = classifier.predict(test)
        print("tests")
        print(testcat)
        print(intlist)
        test_loss, accuracy_score = classifier.evaluate(test, testcat)
        if isinstance(classifier, tf.keras.Model):
            print(classifier.metrics_names)
            print(classifier.summary())
        print("test_loss")
        print(test_loss)
        print(accuracy_score)
        print(type(accuracy_score))
        print("\nTest Accuracy: {0:f}\n".format(accuracy_score))
        return accuracy_score

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
        return os.path.isfile(self.getpath(myobj) + myobj.filename + ".ckpt.index")
    
    def do_learntestclassify(self, queue, request):
      with tf.compat.v1.Session() as sess:
      #with tf.compat.v1.get_default_session() as sess:
        #tf.logging.set_verbosity(tf.logging.FATAL)
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        #myobj = json.loads(request, object_hook=lt.LearnTest)
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, size) = self.gettraintest(myobj, config)
        myobj.size = size
        exists = self.exists(myobj)
        # load model if:                                                               # exists and not dynamic and wantclassify
        if exists and not self.wantDynamic(myobj) and self.wantClassify(myobj):
            #with tf.get_default_session() as sess:
            if Model.Model.localsave():
                # dummy variable to allow saver
                model = Model.Model(myobj, config)
                saver = tf.compat.v1.train.Saver()
                print("Restoring")
                saver.restore(sess, self.getpath(myobj) + myobj.filename + ".ckpt")
                print("Restoring done")
            else:
                model = Model.Model(myobj, config)
        else:
            model = Model.Model(myobj, config)
        classifier = model
        if config.name == "rnnnot":
            train = np.transpose(train, [1, 0, 2])
            test = np.transpose(test, [1, 0, 2])
            
        accuracy_score = None
        if self.wantLearn(myobj):
            accuracy_score = self.do_learntestinner(myobj, classifier, train, traincat, test, testcat)
        #print(type(classifier))

        #print("neuralnetcommand")
        #print(myobj.neuralnetcommand.mlclassify, myobj.neuralnetcommand.mllearn, myobj.neuralnetcommand.mldynamic)
        # save model if                                                                # not dynamic and wantlearn
        if not self.wantDynamic(myobj) and self.wantLearn(myobj):
            #with tf.compat.v1.get_default_session() as sess:
            if Model.Model.localsave():
                saver = tf.compat.v1.train.Saver()
                print("Saving")
                save_path = saver.save(sess, self.getpath(myobj) + myobj.filename + ".ckpt")

        (intlist, problist) = (None, None)
        if self.wantClassify(myobj):
            (intlist, problist) = self.do_classifyinner(myobj, classifier)
        #print(len(intlist))
        print(intlist)
        print(problist)
        classifier.tidy()
        del classifier
        dt = datetime.now()
        if not accuracy_score is None:
            accuracy_score = float(accuracy_score)
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        queue.put(Response(json.dumps({"classifycatarray": intlist, "classifyprobarray": problist, "accuracy": accuracy_score}), mimetype='application/json'))

    def do_dataset(self, queue, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, size, classes) = mydatasets.getdataset(myobj, config)
        myobj.size = size
        myobj.classes = classes
        model = Model.Model(myobj, config)
        classifier = model
        accuracy_score = self.do_learntestinner(myobj, classifier, train, traincat, test, testcat)
        global dictclass
        #dictclass[str(myobj.modelInt) + myobj.period + myobj.modelname] = classifier
        #global dicteval
        #dicteval[myobj.modelname] = float(accuracy_score)
        #print("seteval" + str(myobj.modelname))
        
        classifier.tidy()
        del classifier
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        queue.put(Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json'))
        #return Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json')

    def getpath(self, myobj):
        if hasattr(myobj, 'path'):
            return myobj.path + '/'
        return '/tmp/'

    def do_filename(self, queue, request):
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        exists = self.exists(myobj)
        queue.put(Response(json.dumps({"exists": exists}), mimetype='application/json'))
