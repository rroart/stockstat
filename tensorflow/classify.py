import learntest as lt
import device
import os

import pandas as pd
import tensorflow as tf
import numpy as np

import importlib

import json
from nameko.web.handlers import http
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
        model = Model.Model(myobj, config)
        classifier = model
        (train, traincat, test, testcat) = self.gettraintest(myobj)
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

    def gettraintest(self, myobj):
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
        #print("classes")
        #print(myobj.classes)
        #print("cwd")
        #print(os.getcwd())
        #print(classifier)
        #print("train", train);
        #print(traincat)
        return train, traincat, test, testcat
    
    def do_learntestinner(self, myobj, classifier, train, traincat, test, testcat):
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
        print("test_loss")
        print(test_loss)
        print(accuracy_score)
        print(type(accuracy_score))
        print("\nTest Accuracy: {0:f}\n".format(accuracy_score))
        return accuracy_score

    def getModel(self, myobj):
        if myobj.modelInt == 1:
            modelname = 'estimator_dnn'
            config = myobj.tensorflowDNNConfig
        if myobj.modelInt == 2:
            modelname = 'estimator_l'
            config = myobj.tensorflowLConfig
        if myobj.modelInt == 3:
            modelname = 'mlp'
            config = myobj.tensorflowMLPConfig
        return config, modelname

    def do_learntestclassify(self, queue, request):
        #tf.logging.set_verbosity(tf.logging.FATAL)
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        #myobj = json.loads(request, object_hook=lt.LearnTest)
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        model = Model.Model(myobj, config)
        classifier = model
        (train, traincat, test, testcat) = self.gettraintest(myobj)
        accuracy_score = self.do_learntestinner(myobj, classifier, train, traincat, test, testcat)
        #print(type(classifier))
        (intlist, problist) = self.do_classifyinner(myobj, classifier)
        #print(len(intlist))
        print(intlist)
        print(problist)
        classifier.tidy()
        del classifier
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        queue.put(Response(json.dumps({"classifycatarray": intlist, "classifyprobarray": problist, "accuracy": float(accuracy_score)}), mimetype='application/json'))

    def do_dataset(self, queue, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, size, classes) = mydatasets.getdataset(myobj)
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

