import learntest as lt
import device

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

pu = device.get_pu()
print("Using pu ", pu)

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
        classifier = dictclass[str(myobj.modelInt) + myobj.period + myobj.mapname]
        intlist = self.do_classifyinner(myobj, classifier)
        print(len(intlist))
        print(intlist)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        return Response(json.dumps({"classifycatarray": intlist}), mimetype='application/json')
        
    def do_classifyinner(self, myobj, classifier):
        array = np.array(myobj.classifyarray, dtype='f')
        def get_classifier_inputs():
            x = tf.constant(array)
            return x
            
        predictions = list(classifier.predict_classes(input_fn=get_classifier_inputs))
        intlist = []
        for prediction in predictions:
            # NOTE changing prediction back again. see other NOTE
            prediction = int(prediction + 1)
            intlist.append(prediction)
        return intlist

    def do_learntest(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (classifier, accuracy_score) = self.do_learntestinner(myobj)
        global dictclass
        dictclass[str(myobj.modelInt) + myobj.period + myobj.mapname] = classifier
        global dicteval
        dicteval[str(myobj.modelInt) + myobj.period + myobj.mapname] = float(accuracy_score)
        print("seteval" + str(myobj.modelInt) + myobj.period + myobj.mapname)
        
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        return Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json')

    def do_learntestinner(self, myobj):
        array = np.array(myobj.trainingarray, dtype='f')
        cat = np.array(myobj.trainingcatarray, dtype='i')
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
            with tf.device(pu):
                classifier = tf.contrib.learn.DNNClassifier(feature_columns=feature_columns,
                                                            hidden_units=[10, 20, 10],
                                                            n_classes=myobj.outcomes,
                                                            model_dir="/tmp/tf" + str(myobj.modelInt) + myobj.period + myobj.mapname + str(count))
        if myobj.modelInt == 2:
            #print("mod2")
            with tf.device(pu):
                classifier = tf.contrib.learn.LinearClassifier(
                    feature_columns=feature_columns,
                    model_dir="/tmp/tf" + str(myobj.modelInt) + myobj.period + myobj.mapname + str(count))
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
        return classifier, accuracy_score
    
    def do_learntestclassify(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (classifier, accuracy_score) = self.do_learntestinner(myobj)
        intlist = self.do_classifyinner(myobj, classifier)
        print(len(intlist))
        print(intlist)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        return Response(json.dumps({"classifycatarray": intlist, "accuracy": float(accuracy_score)}), mimetype='application/json')
