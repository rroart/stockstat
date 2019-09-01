#!/usr/bin/python3
# helloworld.py

import pandas as pd
import tensorflow as tf
import numpy as np

import json
from nameko.web.handlers import http
from datetime import datetime
from werkzeug.wrappers import Response

global dicteval
dicteval = {}
global dictclass
dictclass = {}
global count
count = 0

class LearnTest:
    def __init__(self, array):
        vars(self).update(array)
    def __init__(self, cat):
        vars(self).update(cat)
    def __init__(self, listlist):
        vars(self).update(listlist)
    def __init__(self, modelInt):
        vars(self).update(modelInt)
    def __init__(self, size):
        vars(self).update(size)
    def __init__(self, period):
        vars(self).update(period)
    def __init__(self, modelname):
        vars(self).update(modelname)
    def __init__(self, classes):
        vars(self).update(classes)

class HttpService:
    name = "http_service"
    @http('POST', '/eval')
    def do_eval(self, request):
        global dicteval
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=LearnTest)
        print("geteval" + str(myobj.modelInt) + myobj.period + myobj.modelname)
        accuracy_score = dicteval[str(myobj.modelInt) + myobj.period + myobj.modelname]
        return Response(json.dumps({"prob": accuracy_score}), mimetype='application/json')
    @http('POST', '/classify')
    def do_classify(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=LearnTest)
        array = np.array(myobj.array, dtype='f')
        global dictclass
        classifier = dictclass[str(myobj.modelInt) + myobj.period + myobj.modelname]
        def get_classifier_inputs():
            x = tf.constant(array)
            return x
            
        predictions = list(classifier.predict_classes(input_fn=get_classifier_inputs))
        intlist = []
        for prediction in predictions:
            # NOTE changing prediction back again. see other NOTE
            prediction = int(prediction + 1)
            intlist.append(prediction)
            
        print(len(intlist))
        print(intlist)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        return Response(json.dumps({"cat": intlist}), mimetype='application/json')
    @http('POST', '/learntest')
    def do_learntest(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=LearnTest)
        array = np.array(myobj.array, dtype='f')
        cat = np.array(myobj.cat, dtype='i')
        # NOTE class range 1 - 4 will be changed to 0 - 3
        # to avoid risk of being classified as 0 later
        cat = cat - 1
        print(len(cat))
        print(cat)
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
        feature_columns = [tf.contrib.layers.real_valued_column("", dimension=25)]
        global count
        count = count + 1                                                
        if myobj.modelInt == 1:
            #print("mod1")
            classifier = tf.contrib.learn.DNNClassifier(feature_columns=feature_columns,
                                                        hidden_units=[10, 20, 10],
                                                        n_classes=myobj.classes,
                                                        model_dir="/tmp/tf" + str(myobj.modelInt) + myobj.period + myobj.modelname + str(count))
        if myobj.modelInt == 2:
            #print("mod2")
            classifier = tf.contrib.learn.LinearClassifier(
                feature_columns=feature_columns,
                model_dir="/tmp/tf" + str(myobj.modelInt) + myobj.period + myobj.modelname + str(count))
        global dictclass
        dictclass[str(myobj.modelInt) + myobj.period + myobj.modelname] = classifier
        def get_train_inputs():
            x = tf.constant(train)
            y = tf.constant(traincat)
            return x, y
            
        classifier.fit(input_fn = get_train_inputs, steps=2000)
        # Evaluate accuracy.
        def get_test_inputs():
            x = tf.constant(test)
            y = tf.constant(testcat)
            return x, y
            
        accuracy_score = classifier.evaluate(input_fn = get_test_inputs, steps=1)["accuracy"]

        print("\nTest Accuracy: {0:f}\n".format(accuracy_score))
        global dicteval
        dicteval[str(myobj.modelInt) + myobj.period + myobj.modelname] = float(accuracy_score)
        print("seteval" + str(myobj.modelInt) + myobj.period + myobj.modelname)
        
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        return Response(json.dumps({"prob": float(accuracy_score)}), mimetype='application/json')
