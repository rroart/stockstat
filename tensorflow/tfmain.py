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

def get_train_inputs():
    x = tf.constant(train)
    y = tf.constant(test)
    return x, y
            
class MyClass:
#    def __init__( self, text, arr ):
    def __init__( self, text ):
        vars(self).update(text)
    def __init__( self, arr ):
        vars(self).update(arr)
#    text
#    arr

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
    def __init__(self, mapname):
        vars(self).update(mapname)
    def __init__(self, outcomes):
        vars(self).update(outcomes)

class HttpService:
    name = "http_service"
    @http('GET', '/getme/<int:value>')
    def get_method(self, request, value):
        return json.dumps({'value': value})
    @http('POST', '/getme2/<int:value>')
    def get_method2(self, request, value):
        df_train = pd.read_csv(tf.gfile.Open("adult.data"))
        return json.dumps({'value': df_train})
    @http('POST', '/eval')
    def do_eval(self, request):
        global dicteval
        print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=LearnTest)
        print("eval" + str(myobj.modelInt) + myobj.period + myobj.mapname)
        accuracy_score = dicteval[str(myobj.modelInt) + myobj.period + myobj.mapname]
        return Response(json.dumps({"prob": accuracy_score}), mimetype='application/json')
    @http('POST', '/classify')
    def do_classify(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
#        print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=LearnTest)
#        print(myobj.array)
#        print(type(myobj.array))
#        print(request.get_data(as_text=True))
        array = np.array(myobj.array, dtype='f')
        global dictclass
        classifier = dictclass[str(myobj.modelInt) + myobj.period + myobj.mapname]
        print(array.shape)
        predictions = list(classifier.predict(x=array))
        intlist = []
        for prediction in predictions:
            prediction = int(prediction)
            #prediction.astype(np.int)
            intlist.append(prediction)
            
        print(type(intlist[0]))
        print(intlist)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        return Response(json.dumps({"cat": intlist}), mimetype='application/json')
    @http('POST', '/learntest')
    def do_learntest(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
#        print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=LearnTest)
        print(type(myobj))
        print(myobj)
        print(myobj.outcomes)
#        print (myobj.text)
        print ("array")
        print (type(myobj.array))
#        print (myobj.array)
#        print (type(myobj.array[0][2]))
#        print (myobj.array[0][2])
#        print (type(myobj.array[0][0]))
        array = np.array(myobj.array, dtype='f')
        cat = np.array(myobj.cat, dtype='i')
        (lenrow, lencol) = array.shape
        half = round(lenrow / 2)
        print("half", half)
#        print(array)
        print(type(array))
        print(array.shape)
        packed = tf.stack(array)
        print(type(packed))
        df = pd.DataFrame(array)
        print(type(array), array.shape)
        print(type(cat), cat.shape)
        train = array[:half, :]
        test = array[half:, :]
        traincat = cat[:half]
        testcat = cat[half:]
#        trainx = train[:, :(lencol - 1)]
#        trainy = train[:, (lencol - 1):]
        print(traincat)
        print(type(test))
        feature_columns = [tf.contrib.layers.real_valued_column("", dimension=25)]
#        myobj.modelInt = 1
        print(type(myobj.modelInt),myobj.modelInt)
        if myobj.modelInt == 1:
            print("mod1")
            classifier = tf.contrib.learn.DNNClassifier(feature_columns=feature_columns,
                                            hidden_units=[10, 20, 10],
                                            n_classes=myobj.outcomes + 1,
#                                            n_classes=4,
                                                        model_dir="/tmp/tf" + str(myobj.modelInt) + myobj.period + myobj.mapname)
        if myobj.modelInt == 2:
            print("mod2")
            classifier = tf.contrib.learn.LinearClassifier(
                feature_columns=feature_columns,
                model_dir="/tmp/tf" + str(myobj.modelInt) + myobj.period + myobj.mapname)
        global dictclass
        dictclass[str(myobj.modelInt) + myobj.period + myobj.mapname] = classifier
        #        classifier.fit(input_fn=get_train_inputs, steps=2000)
        classifier.fit(x = train, y = traincat, steps=2000)
        # Evaluate accuracy.
        accuracy_score = classifier.evaluate(x = test, y = testcat, steps=1)["accuracy"]

        print("\nTest Accuracy: {0:f}\n".format(accuracy_score))
        global dicteval
        dicteval[str(myobj.modelInt) + myobj.period + myobj.mapname] = int(accuracy_score)
        print("seteval" + str(myobj.modelInt) + myobj.period + myobj.mapname)
        
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        return Response(json.dumps({"outcomes": 5}), mimetype='application/json')
        
#        return json.dumps({"outcomes": 5})

#        return json.dumps(myobj)
#        return u"received: {}".format(request.get_data(as_text=True))
