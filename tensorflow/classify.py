import learntest as lt
import device
import os

import pandas as pd
import tensorflow as tf
import numpy as np

import json
from nameko.web.handlers import http
from datetime import datetime
from werkzeug.wrappers import Response
import shutil

from multiprocessing import Queue

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
        del dictclass[str(myobj.modelInt) + myobj.period + myobj.mapname]
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
        def get_classifier_inputs_2():
            x = tf.constant(array)
            return x
        get_classifier_inputs = tf.estimator.inputs.numpy_input_fn(
                x = { "features": array },
                shuffle=False
                )
            
        #predictions = list(classifier.predict(input_fn=get_classifier_inputs)["classes"])
        #predictions = classifier.predict(input_fn=get_classifier_inputs)
        #for prediction in predictions:
        #    print(prediction)
        predictions = classifier.predict(input_fn=get_classifier_inputs)
        intlist = []
        problist = []
        for prediction in predictions:
            class_id = prediction['class_ids'][0]
            # NOTE changing prediction back again. see other NOTE
            probability = float(prediction['probabilities'][class_id])
            class_id = int(class_id + 1)
            intlist.append(class_id)
            problist.append(probability)
        #shutil.rmtree("/tmp/tf" + str(myobj.modelInt) + myobj.period + myobj.mapname + str(count))
        shutil.rmtree(classifier.model_dir)
        del predictions
        del classifier
        return intlist, problist

    def do_learntest(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (classifier, accuracy_score) = self.do_learntestinner(myobj)
        global dictclass
        #dictclass[str(myobj.modelInt) + myobj.period + myobj.mapname] = classifier
        global dicteval
        dicteval[str(myobj.modelInt) + myobj.period + myobj.mapname] = float(accuracy_score)
        print("seteval" + str(myobj.modelInt) + myobj.period + myobj.mapname)
        
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        return Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json')

    def do_learntestinner(self, myobj):
        tensorflowDNNConfig = myobj.tensorflowDNNConfig
        tensorflowLConfig = myobj.tensorflowLConfig
        array = np.array(myobj.trainingarray, dtype='f')
        cat = np.array(myobj.trainingcatarray, dtype='i')
        # NOTE class range 1 - 4 will be changed to 0 - 3
        # to avoid risk of being classified as 0 later
        cat = cat - 1
        #print(len(cat))
        #print(array)
        #print(cat)
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
        #feature_columns = [ '', '', '', '' ]
        #feature_columns = [ tf.feature_column.numeric_column("", shape=1) for num in range(myobj.size)]
        #feature_columns = [ tf.feature_column.numeric_column(str(num), shape=1) for num in range(myobj.size)]
        feature_columns = [ tf.feature_column.numeric_column("features", shape=[myobj.size] ) ]
        print(feature_columns)
        #feature_columns = tf.feature_column.numeric_column("", shape=(myobj.size,))
        #print("d0")
        #datasettrain0 = tf.constant(train)
        #print(datasettrain0)
        #datasettrain0 = tf.data.Dataset.from_tensor_slices(train)
        #iterator = datasettrain0.make_one_shot_iterator()
        #f1, f2 = iterator.get_next()
        #print(f1)
        #print(f2)
        #print(datasettrain0)
        #datasetme = datasettrain0.map(_parse)
        #datasettrain0 = tf.convert_to_tensor(train)
        #iterator = datasettrain0.make_one_shot_iterator()
        #print(datasettrain0)
        #datasettrain0 = tf.map_fn(lambda x: x, datasettrain0)
        #iterator = datasettrain0.make_one_shot_iterator()
        #print(datasettrain0)
        #datasetcat = tf.cast(traincat, tf.int32)
        #iterator = datasettrain0.make_one_shot_iterator()
        #print(datasetcat)
        #d = dict(zip(feature_columns, datasettrain0))
        #print("d")
        #print(d)
        #return d, datasetcast
        #df = pd.DataFrame(array, dtype=np.float32)
        #df = pd.DataFrame(array, dtype=np.float32, columns =[ "{}".format(i) for i in range(myobj.size) ])
        #datasettrain = tf.data.Dataset.from_tensor_slices((dict(df), cat))
        #datasettrain = tf.data.Dataset.from_tensor_slices((dict(zip(feature_columns, datasettrain0)), traincat))
        #datasettrain = tf.data.Dataset.from_tensor_slices((myobj.trainingarray, myobj.trainingcatarray))
        #datasettest = tf.data.Dataset.from_tensor_slices((test, testcat))
        #print("shpaes")
        #print(dataset)
        #(train,test) = tf.split(dataset, num_or_size_splits=2, axis=1)
        #print(datasettrain)
        #print(datasettest)
        size = 0
        #feature_columns = [tf.contrib.layers.real_valued_column("", dimension=myobj.size)]
        global count
        count = count + 1
        steps = 0
        print("outcomes")
        print(myobj.outcomes)
        #print("cwd")
        #print(os.getcwd())
        if myobj.modelInt == 1:
            #print("mod1")
            #print("hidden")
            #print(tensorflowDNNConfig.hiddenunits)
            #print(type(tensorflowDNNConfig.hiddenunits))
            #hidden_units = json.loads(tensorflowDNNConfig.hiddenunits)
            hidden_units = tensorflowDNNConfig.hiddenunits
            #print(hidden_units)
            #print(type(hidden_units))
            with tf.device(pu):
                classifier = tf.estimator.DNNClassifier(feature_columns=feature_columns,
                                                            hidden_units=hidden_units,
                                                            n_classes=myobj.outcomes)
                steps = tensorflowDNNConfig.steps
        if myobj.modelInt == 2:
            #print("mod2")
            with tf.device(pu):
                classifier = tf.estimator.LinearClassifier(
                    feature_columns=feature_columns,
                    n_classes=myobj.outcomes)
                steps = tensorflowLConfig.steps
        #print("steps")
        #print(steps)
        #print(type(array))
        #print(type(cat))
        #print(np.array(array))
        #print(np.array(cat))
        #print(myobj.size)
        get_train_inputs = tf.estimator.inputs.numpy_input_fn(
                x = { "features": train },
                y = traincat,
                num_epochs=None,
                shuffle=True
                )
        #print(get_train_inputs())
        get_test_inputs = tf.estimator.inputs.numpy_input_fn(
                x = { "features": test },
                y = testcat,
                num_epochs=None,
                shuffle=True
                )
        def get_train_inputs_m():
            iterator = datasettrain.make_one_shot_iterator()
            features, labels = iterator.get_next()
            print(features)
            print(labels)
            return features, labels
            #return datasettrain
            
        def get_train_inputs_n():
            #dataset = datasettrain.batch(128)
            #iterator = dataset.make_one_shot_iterator()
            #features, labels = iterator.get_next()
            print(features)
            print(labels)
            return features, labels
            
        #try:
        #    print("cat")
        #    print(traincat)
        #    print(myobj.outcomes)
        classifier.train(input_fn = get_train_inputs, steps=steps)
        #except tensorflow.python.framework.errors_impl.InvalidArgumentError:
        #    print("exception")
        #    print(myobj.outcomes)
        #    print(traincat)
        #    print("endexc")
        #except:
        #    print("exception2")
        #    print(myobj.outcomes)
        #    print(traincat)
        #    print("endexc")
        # Evaluate accuracy.
        def get_test_inputs_2():
            x = tf.constant(test)
            y = tf.constant(testcat)
            return x, y

        accuracy_score = classifier.evaluate(input_fn = get_test_inputs, steps=1)["accuracy"]
        print(accuracy_score)
        print(type(accuracy_score))
        print("\nTest Accuracy: {0:f}\n".format(accuracy_score))
        return classifier, accuracy_score
    
    def do_learntestclassify(self, queue, request):
        #tf.logging.set_verbosity(tf.logging.FATAL)
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        #myobj = json.loads(request, object_hook=lt.LearnTest)
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        (classifier, accuracy_score) = self.do_learntestinner(myobj)
        (intlist, problist) = self.do_classifyinner(myobj, classifier)
        print(len(intlist))
        print(intlist)
        print(problist)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        queue.put(Response(json.dumps({"classifycatarray": intlist, "classifyprobarray": problist, "accuracy": float(accuracy_score)}), mimetype='application/json'))
