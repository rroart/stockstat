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
    if hasattr(tf.keras, '__version__'):
        print("Tensorflow Keras", tf.keras.__version__)
except:
    import sys,traceback
    traceback.print_exc(file=sys.stdout)
    
class Classify:
    def do_eval(self, request):
        """Handle an evaluation request.

        Expects a Flask/Werkzeug request whose body is a JSON representation
        that can be deserialized into a LearnTest object. Reads the model
        evaluation score from the global `dicteval` map and returns a JSON
        response with the accuracy.

        Args:
            request: Werkzeug/Flask request with JSON body representing
                     LearnTest fields (including modelInt, period, modelname).

        Returns:
            Response: a Werkzeug Response with JSON {"accuracy": <float>}.
        """
        global dicteval
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        print("geteval" + str(myobj.modelInt) + myobj.period + myobj.modelname)
        accuracy_score = dicteval[str(myobj.modelInt) + myobj.period + myobj.modelname]
        return Response(json.dumps({"accuracy": accuracy_score}), mimetype='application/json')

    def do_classify(self, request):
        """Handle an online classification request.

        Deserializes the incoming JSON into a LearnTest object, retrieves a
        classifier instance from the shared `dictclass` mapping, runs
        classification and probability prediction via `do_classifyinner`,
        and returns the results as JSON.

        Args:
            request: Werkzeug/Flask request with JSON body including fields
                     required for classification (modelname, classifyarray, etc.).

        Returns:
            Response: JSON response with keys `classifycatarray` and
                      `classifyprobarray` containing predicted classes and
                      probabilities respectively.
        """
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        classify = not hasattr(myobj, 'classify') or myobj.classify == True
        global dictclass
        classifier = dictclass[myobj.modelname]
        del dictclass[myobj.modelname]
        (intlist, problist) = self.do_classifyinner(myobj, classifier, config, )
        #print(len(intlist))
        #print(intlist)
        #print(problist)
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        
        return Response(json.dumps({"classifycatarray": intlist, "classifyprobarray": problist }), mimetype='application/json')
        
    def do_classifyinner(self, myobj, classifier, config, classify):
        """Core classification logic used by higher level handlers.

        This function converts the provided classifyarray to a numpy array and
        uses `classifier.predict` to obtain class indices and probability
        estimates. It handles both sequential multi-step classification when
        `classify` is False (prediction of a sequence of rolling predictions),
        and single-shot classification when `classify` is True.

        Args:
            myobj: LearnTest-like object containing `classifyarray`, `classes`,
                   `modelInt` and other attributes used to reshape data.
            classifier: model wrapper that implements `predict(array)`.
            config: configuration object with flags (e.g. `binary`).
            classify: bool, when True run single-step classification, when
                      False run iterative rolling classification.

        Returns:
            tuple: (intlist, problist) where intlist is a list (or nested
                   list) of predicted class indices and problist is the
                   corresponding probability list/array.
        """
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
                #print("ar", array)
                (intlist, problist) = classifier.predict(array)
                intlist = np.array(intlist)
                #print("pr", intlist)
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
            #print("Classify", array)
            (intlist, problist) = classifier.predict(array)
            if config.binary:
                print("problist", problist)
                intlist = np.round(problist, 0)
                intlist = np.int_(intlist)
                problist = np.array(problist)
                # make new probabilities based on the rounded values, 0 with 1 - val, 1 with val
                print(type(problist), type(intlist))
                print("problist", problist)
                problist =  (1 - problist) * (1 - intlist) + problist * intlist # todo
                print("problist", problist)
                problist = problist.tolist()
                intlist = intlist.tolist()

        if classify and not self.zero(myobj):
            intlist = np.array(intlist)
            intlist = intlist + 1
            intlist = intlist.tolist()
        return intlist, problist

    def do_learntest(self, queue, request):
        """Run a learn/test job based on an incoming request.

        This reads a LearnTest JSON from the request, constructs the model,
        prepares datasets, runs training/evaluation (via `do_learntestinner`)
        and places the result into the provided multiprocessing `queue`.

        Args:
            queue: multiprocessing.Queue to put result dict into.
            request: Werkzeug/Flask request containing LearnTest JSON in body.
        """
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        classify = not hasattr(myobj, 'classify') or myobj.classify == True
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, shape, val, valcat) = self.gettraintest(myobj, config, classify)
        #myobj.size = size
        model = Model.Model(myobj, config, classify, shape)
        classifier = model
        (accuracy_score, loss, train_accuracy_score, train_loss, val_accuracy, val_loss) = self.do_learntestinner(myobj, config, classifier, train, traincat, test, testcat, classify, val, valcat)
        global dictclass
        #dictclass[str(myobj.modelInt) + myobj.period + myobj.modelname] = classifier
        #global dicteval
        #dicteval[myobj.modelname] = float(accuracy_score)
        #print("seteval" + str(myobj.modelname))
        
        classifier.tidy()
        del classifier
        dt = datetime.now()
        #print ("millis ", (dt.timestamp() - timestamp)*1000)
        queue.put({"accuracy": float(accuracy_score), "trainaccuracy": float(train_accuracy_score), 'train_loss' : train_loss, 'valaccuracy' : val_accuracy, 'val_loss' : val_loss})
        #return Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json')

    def getSlide(self, inputs, labels, myobj, config):
        """Create sliding-window samples for 2D input arrays.

        Converts a 2-dimensional time-series array into multiple sliding-window
        input/output pairs according to `myobj.size`, `config.slide_stride` and
        `myobj.classes`.

        Args:
            inputs: numpy array shaped (batch, sequence_length) or convertible to it.
            labels: ignored/currently unused; kept for API compatibility.
            myobj: object containing `size` (window width) and `classes`.
            config: configuration that may include `slide_stride`.

        Returns:
            tuple: (inputs, labels) where inputs is the transformed input array
                   (may be reshaped for CNN/MLP models) and labels are the
                   targets corresponding to each sliding window.
        """
        if not len(inputs.shape) == 2:
            print("getSlide expects 2D array")
        #input = torch.from_numpy(np.array([[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20]]]))
        #inputs = torch.from_numpy(inputs)
        inputs = np.array(inputs, dtype='f')
        size = inputs.shape
        if  hasattr(config, 'slide_stride'):
            sliding_window_stride = config.slide_stride
        else:
            sliding_window_stride = 2
        print("shape", inputs.shape)
        sliding_window_width = myobj.size
        #print(type(size))
        #print(size)
        mysize = size[0]
        sequence_length = size[1]
        print("arange", sequence_length, sliding_window_width, sliding_window_stride, myobj.classes)
        arange = int((sequence_length - (sliding_window_width) - myobj.classes) / sliding_window_stride) + 1
        print("arange", arange)
        print("size", size, mysize, sequence_length, sliding_window_width, sliding_window_stride, arange)
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
        print(inputs)
        print(labels)
        #myobj.classes = 1
        if config.name == 'mlp' or config.name == 'lir':
            ii = 1
        else:
            #print("notmlp")
            #print(splitInput.shape)
            #myobj.size = (1, sliding_window_width)
            inputs = inputs.reshape(mysize * arange, 1, sliding_window_width)
            #print(splitInput.shape)
        return inputs, labels

    def getSlide2(self, inputs, labels, myobj, config):
        """Create sliding-window samples for 3D arrays (batch, minibatch, seq_len).

        This is similar to `getSlide` but operates on inputs shaped
        (mysize, minibatch, sequence_length) producing a 3D splitInput and
        corresponding splitTarget shaped to match model expectations.

        Args:
            inputs: numpy array convertible to float of shape (mysize, minibatch, seq_len).
            labels: unused placeholder kept for interface compatibility.
            myobj: object with `size` and `classes` attributes.
            config: configuration containing `slide_stride`.

        Returns:
            tuple: (inputs, labels) transformed for model consumption.
        """
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

    # not used
    def getSlideT(self, inputs, labels, myobj, config):
        """TensorFlow variant of sliding-window generation (kept for reference).

        Works like `getSlide2` but uses TensorFlow tensors and Variable/assign.
        This function is not currently used by the codebase but kept for
        completeness.

        Args:
            inputs: array-like convertible to tf.Tensor with shape
                    (mysize, minibatch, seq_len).
            labels: unused placeholder.
            myobj: object with `size` and `classes`.
            config: configuration with `slide_stride`.

        Returns:
            tuple: (inputs, labels) as TensorFlow tensors.
        """
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
        """Check whether classes should be treated as zero-based.

        Returns True only if `myobj` has attribute `zero` set to True. The
        project convention uses 1-based classes by default and sets `zero` to
        True to request zero-based handling.

        Args:
            myobj: object that may contain attribute `zero`.

        Returns:
            bool: True if zero-based behavior requested, False otherwise.
        """
        return hasattr(myobj, 'zero') and myobj.zero == True

    def gettraintest(self, myobj, config, classify, size = 0.2, size2 = 0.25):
        """Prepare train/validation/test arrays from provided training data.

        This reads `myobj.trainingarray` (and optional training categories),
        applies any necessary transpositions for CNN models, optionally
        generates sliding-window inputs for non-classification tasks and
        splits data into train/val/test sets according to `size` and `size2`.

        Args:
            myobj: LearnTest-like object containing `trainingarray` and
                   optional `trainingcatarray`, `testarray` and `testcatarray`.
            config: model configuration object used for reshape rules.
            classify: boolean flag indicating whether the target is a
                      classification task.
            size: float fraction of data reserved for testing (default 0.2).
            size2: float fraction for validation split inside training (default 0.25).

        Returns:
            tuple: (train, traincat, test, testcat, shape, val, valcat)
        """
        #mydim = myobj.size
        array = np.array(myobj.trainingarray, dtype='f')
        shape = array.shape
        print("Shape", array.shape)
        #print("grr", array[0])
        #layer = keras.layers.Normalization(axis=None) #, invert=True)
        #layer = keras.layers.Normalization(axis=1) #, invert=True)
        #layer.adapt(array)
        #array = layer(array)
        print("Shape", array.shape)
        array = self.transpose_cnn(myobj, config, array)

        print("Shapes0", array.shape)
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
            #mydim = myobj.size
            return inputs, labels, inputs, labels, inputs.shape, inputs, labels
        if hasattr(myobj, 'testarray') and hasattr(myobj, 'testcatarray'):
            print("hastestarray")
            test = np.array(myobj.testarray, dtype='f')
            testcat = np.array(myobj.testcatarray, dtype='i')

            lenrow = array.shape[0]
            half = round(lenrow * 1)
            train, traincat, val, valcat = self.splitarray(half, size2, array, cat)
            # NOTE class range 1 - 4 will be changed to 0 - 3
            # to avoid risk of being classified as 0 later
            if not self.zero(myobj):
                testcat = testcat - 1
        else:
            lenrow = array.shape[0]
            half = round(lenrow * (1 - size))
            train = array[:half, :]
            test = array[half:, :]
            traincat = cat[:half]
            testcat = cat[half:]
            print("half", half, lenrow)

            train, traincat, val, valcat = self.splitarray(half, size2, train, traincat)

            if len(cat) == 1:
                train = array
                test = array
                val = array
                traincat = cat
                testcat = cat
                valcat = cat
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
        print("Lens", len(traincat), len(valcat), len(testcat))
        #return array, cat, array, cat, mydim, array, cat
        return train, traincat, test, testcat, train.shape, val, valcat

    def transpose_cnn(self, myobj, config, array):
        """Transpose input arrays into TensorFlow channel-last format when needed.

        Handles conversion from (N, C, L) or (N, C, H, W) Java-style ordering to
        TensorFlow's (N, L, C) or (N, H, W, C) channel-last format for cnn/cnn2.
        If no transposition is required, the input is returned unchanged.

        Args:
            myobj: object possibly containing `dataset` attribute used to skip transposition.
            config: model configuration with `name` property (e.g. 'cnn', 'cnn2').
            array: numpy array to be transposed if necessary.

        Returns:
            numpy.array: transposed array appropriate for TensorFlow models.
        """
        # input from java is (N, C, L) and (N, C, H, W)
        # input from dataset is already the default
        # tensorflow default (and working) format is (N, L, C) and (N, H, W, C)
        # TODO no transpose with dataset
        if config.name == "cnn2" and getattr(myobj, 'dataset', None) is None:
            print("cnn2 shape")
            print(array.shape)
            if len(array.shape) == 3:
                print("sh0", array.shape)
                #array = array.reshape(array.shape[0], array.shape[1], array.shape[2], 1)
                print("sh1", array.shape)
            else:
                # array = np.transpose(array, [0, 3, 2, 1])
                array = np.transpose(array, [0, 2, 3, 1])
                #array = array.reshape(array.shape[0], 1, array.shape[1], array.shape[2])

            print(array.shape)

        if config.name == "cnn" and getattr(myobj, 'dataset', None) is None:
            print("cnn shape")
            print(array.shape)
            if len(array.shape) == 3:
                array = np.transpose(array, [0, 2, 1])
                #array = array.reshape(array.shape[0], array.shape[2], array.shape[1])
            #if len(array.shape) == 2:
            #    array = array.reshape(array.shape[0], array.shape[1], 1)
        return array
        # from classify:

        if config.name == "cnn2":  # todo
            print("cnn2 shape")
            array = np.array(myobj.classifyarray, dtype='f')
            print(array.shape)
            # array = np.transpose(array, [0, 3, 2, 1])
            array = np.transpose(array, [0, 2, 3, 1])
            print(array.shape)

    def splitarray(self, half, size, train, traincat):
        """Split a training array into new training and validation sets.

        The function computes a new 'half' index according to the provided
        fraction `size` and then splits `train` and `traincat` into a
        new training portion and a validation portion.

        Args:
            half: integer or float representing the current length to split from.
            size: float fraction used to compute validation portion.
            train: numpy array of training examples.
            traincat: numpy array of training categories.

        Returns:
            tuple: (newtrain, newtraincat, val, valcat)
        """
        half = round(half * (1 - size))
        newtrain = train[:half, :]
        val = train[half:, :]
        newtraincat = traincat[:half]
        valcat = traincat[half:]
        return newtrain, newtraincat, val, valcat

    def do_learntestinner(self, myobj, config, classifier, train, traincat, test, testcat, classify, val = None, valcat = None):
        """Inner training/evaluation routine used by learn/test flows.

        Calls `classifier.train` to perform training, extracts metrics from
        the returned history, runs prediction on the test set and evaluates
        metrics (accuracy, loss) via `classifier.evaluate`.

        Args:
            myobj: LearnTest-like object (used for logging and class handling).
            config: model configuration (used for normalization decisions).
            classifier: model wrapper implementing `train`, `predict`, and `evaluate`.
            train, traincat, test, testcat: numpy arrays for data and labels.
            classify: bool whether classification outputs are desired.
            val, valcat: optional validation arrays.

        Returns:
            tuple: (accuracy_score, test_loss, train_accuracy_score, train_loss, val_accuracy, val_loss)
        """
        #print("ttt", train)
        #print("ttt2", traincat)
        #print("ttt3", test)
        #print("ttt4", testcat)
        #print("shape")
        #print(train.shape)
        #print(traincat.shape)
        #print(classifier.train)
        #if isinstance(classifier.model, tf.keras.Model):
        #    print(classifier.model.summary())
        history = classifier.train(train, traincat, val, valcat)
        train_accuracy_score = history.history.get('accuracy')[-1]
        val_accuracy = history.history.get('val_accuracy')[-1]
        train_loss = history.history.get('loss')[-1]
        val_loss = history.history.get('val_loss')[-1]
        print("history", history.history.keys(), train_accuracy_score, val_accuracy, train_loss, val_loss);
        print(history.history.get('accuracy'))
        print(history.history.get('val_accuracy'))
        print(history.history.get('loss'))
        print(history.history.get('val_loss'))
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

        print("Classify", classify)
        # TODO why one?
        #if config.name == 'lir':
        #    train_loss = classifier.evaluate(train, traincat)
        #    test_loss = classifier.evaluate(test, testcat)
        #    train_accuracy_score = None
        #    accuracy_score = None
        #else:
        #    train_loss, train_accuracy_score = classifier.evaluate(train, traincat)
        #    test_loss, accuracy_score = classifier.evaluate(test, testcat)
        if classify and config.normalize:
            test = test #layerutils.normalize((test, av)
        test_loss, accuracy_score = classifier.evaluate(test, testcat)
        print("Keras model", isinstance(classifier, tf.keras.Model))
        if isinstance(classifier, tf.keras.Model):
            print(classifier.metrics_names)
            print(classifier.summary())
        print("Keras model", isinstance(classifier.model, tf.keras.Model))
        if isinstance(classifier.model, tf.keras.Model):
            print(classifier.model.metrics_names)
            print(classifier.model.summary())
        print("Accuracy train val test", train_accuracy_score, val_accuracy, accuracy_score)
        print("Loss train val test", train_loss, val_loss, test_loss)
        #print("test_loss")
        #print(test_loss)
        #print(accuracy_score)
        #print(type(accuracy_score))
        #print("\nTest Accuracy: {0:f}\n".format(accuracy_score))
        return accuracy_score, test_loss, train_accuracy_score, train_loss, val_accuracy, val_loss

    def getModel(self, myobj):
      """Resolve a model configuration and name from a LearnTest-like object.

      The function inspects either `modelInt` or `modelName` on `myobj` and
      returns the appropriate configuration object stored on `myobj` along
      with the canonical model module name.

      Args:
          myobj: object containing model identifiers such as `modelInt` or `modelName`.

      Returns:
          tuple: (config, modelname)
      """
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
        if myobj.modelInt == 18:
            modelname = 'pqk'
            config = myobj.tensorflowPQKConfig
        if myobj.modelInt == 19:
            modelname = 'vae'
            config = myobj.tensorflowVAEConfig
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
        """Return True when the job requests dynamic (non-saved) models.

        Checks `myobj.neuralnetcommand.mldynamic` if present; defaults to True
        (allow dynamic) when the attribute is absent.
        """
        hasit = hasattr(myobj, 'neuralnetcommand')
        if not hasit or (hasit and myobj.neuralnetcommand.mldynamic):
            return True
        return False

    def wantLearn(self, myobj):
        """Return True when the job requests training (mllearn enabled).

        Checks `myobj.neuralnetcommand.mllearn` if present; defaults to True when
        the attribute is absent.
        """
        hasit = hasattr(myobj, 'neuralnetcommand')
        if not hasit or (hasit and myobj.neuralnetcommand.mllearn):
            return True
        return False

    def wantClassify(self, myobj):
        """Return True when the job requests classification (mlclassify enabled).

        Checks `myobj.neuralnetcommand.mlclassify` if present; defaults to True
        when the attribute is absent.
        """
        hasit = hasattr(myobj, 'neuralnetcommand')
        if not hasit or (hasit and myobj.neuralnetcommand.mlclassify):
            return True
        return False

    def exists(self, myobj):
        """Check whether a saved model file exists for `myobj`.

        Uses `myobj.filename` and `myobj.path` (if present) to determine the
        expected file location. Returns False when `filename` is missing.
        """
        if not hasattr(myobj, 'filename'):
            return False
        if os.path.exists(self.getpath(myobj) + myobj.filename):
            return True
        return os.path.isfile(self.getfullpath(myobj))

    #deprecated
    def existsds(self, myobj, modelname):
        """Deprecated: check dataset-specific saved model presence.

        Returns True if `myobj.dataset` is present and the constructed dataset
        path exists on disk.
        """
        if not hasattr(myobj, 'dataset'):
            return False
        return os.path.isfile(self.getdspath(myobj, modelname))
        return os.path.isfile(self.getdspath(myobj, modelname))

    def getdspath(self, myobj, modelname):
        """Construct dataset model file path from `myobj` and `modelname`.

        If `myobj.dataset` is a list, elements are concatenated to form the
        dataset identifier; otherwise the string value is used directly.

        Args:
            myobj: object with `dataset` and optional `path` attributes.
            modelname: string name of the model.

        Returns:
            str: filesystem path to the dataset-specific model file.
        """
        if isinstance(myobj.dataset, list):
            dataset = str(myobj.dataset[0]) + str(myobj.dataset[1])
        else:
            dataset = myobj.dataset
        return self.getpath(myobj) + modelname + dataset + ".keras"

    def do_learntestclassify(self, queue, myjson):
        """Combined learn/test/classify handler for JSON payloads.

        This function accepts a JSON string (myjson), prepares model and data,
        optionally restores saved models, runs learning and/or classification
        according to flags on the payload, and returns results via the
        provided multiprocessing queue.

        Args:
            queue: multiprocessing.Queue to receive the result dict.
            myjson: JSON string representing LearnTest object.
        """
        print("eager", tf.executing_eagerly())
        #tf.logging.set_verbosity(tf.logging.FATAL)
        dt = datetime.now()
        timestamp = dt.timestamp()
        myobj = json.loads(myjson, object_hook=lt.LearnTest)
        print(myjson)
        classify = not hasattr(myobj, 'classify') or myobj.classify == True
        (config, modelname) = self.getModel(myobj)
        print("Model name", modelname)
        Model = importlib.import_module('model.' + modelname)
        (train, traincat, test, testcat, shape, val, valcat) = self.gettraintest(myobj, config, classify, 0.2, 0.25)
        #myobj.size = size
        if not classify:
            if config.name == 'mlp'or config.name == 'lir':
                ii = 1
            else:
                anarray = np.array(myobj.classifyarray, dtype='f')
                myobj.classifyarray = anarray.reshape(anarray.shape[0], 1, anarray.shape[1])
        exists = self.exists(myobj)
        # load model if:
        # exists and not dynamic and wantclassify
        if exists and not self.wantDynamic(myobj) and self.wantClassify(myobj):
            if Model.Model.localsave():
                # dummy variable to allow saver
                model = Model.Model(myobj, config, classify, shape)
                print("Restoring")
                model.model = tf.keras.models.load_model( self.getfullpath(myobj))
                print("Restoring done")
            else:
                model = Model.Model(myobj, config, classify, shape)
        else:
            model = Model.Model(myobj, config, classify, shape)
        classifier = model
        if config.name == "rnnnot":
            train = np.transpose(train, [1, 0, 2])
            test = np.transpose(test, [1, 0, 2])
            
        accuracy_score = None
        train_accuracy_score = None
        loss = None
        train_loss = None
        val_accuracy = None
        val_loss = None
        if self.wantLearn(myobj):
            (accuracy_score, loss, train_accuracy_score, train_loss, val_accuracy, val_loss) = self.do_learntestinner(myobj, config, classifier, train, traincat, test, testcat, classify, val, valcat)
        #print(type(classifier))

        #print("neuralnetcommand")
        #print(myobj.neuralnetcommand.mlclassify, myobj.neuralnetcommand.mllearn, myobj.neuralnetcommand.mldynamic)
        # save model if                                                                # not dynamic and wantlearn
        if not self.wantDynamic(myobj) and self.wantLearn(myobj):
            if model.localsave():
                print("Saving")
                model.save(self.getfullpath(myobj))

        (intlist, problist) = (None, None)
        if self.wantClassify(myobj):
            #print("here0");
            array = np.array(myobj.classifyarray, dtype='f')
            array = self.transpose_cnn(myobj, config, array)
            myobj.classifyarray = array.tolist()
            (intlist, problist) = self.do_classifyinner(myobj, classifier, config, classify)
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
        queue.put({"classifycatarray": intlist, "classifyprobarray": problist, "accuracy": accuracy_score, "trainaccuracy": train_accuracy_score, "loss": loss, 'train_loss' : train_loss, 'valaccuracy' : val_accuracy, 'val_loss' : val_loss, "gpu" : self.hasgpu() })

    def do_dataset(self, queue, myjson):
        """Handle dataset-based training and optional classification.

        Loads datasets via `mydatasets.getdataset` (or `mydatasetsq`) and then
        constructs, trains, and optionally classifies with the appropriate
        model. Results are placed on the provided multiprocessing queue.

        Args:
            queue: multiprocessing.Queue to receive results.
            myjson: JSON string representing LearnTest-like object.
        """
        dt = datetime.now()
        timestamp = dt.timestamp()
        print(myjson)
        myobj = json.loads(myjson, object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        if hasattr(myobj, 'normalizevalue'):
            import mydatasetsq
            (ds, meta) = mydatasetsq.getdataset(myobj, config, self)
        else:
            (ds, meta) = mydatasets.getdataset(myobj, config, self)
        if hasattr(ds, 'train'):
            train = np.array(ds.train)
            myobj.trainingarray = ds.train
            myobj.trainingcatarray = ds.traincat
            #myobj.size = meta.size # TODO
            myobj.classes = meta.classes
            (ds.train, ds.traincat, ds.test, ds.testcat, shape, val, valcat) = self.gettraintest(myobj, config, meta.classify)
        #print("classez2", myobj.classes)
        model = Model.Model(myobj, config, meta.classify, shape)
        exists = False # not yet: self.exists(myobj)
        # load model if:
        # exists and not dynamic and wantclassify
        if exists and not self.wantDynamic(myobj) and self.wantClassify(myobj):
            if Model.Model.localsave():
                # dummy variable to allow saver
                model = Model.Model(myobj, config, meta.classify, shape)
                print("Restoring")
                model.model = tf.keras.models.load_model( self.getfullpath(myobj))
                print("Restoring done")
            else:
                model = Model.Model(myobj, config, meta.classify, shape)
        else:
            model = Model.Model(myobj, config, meta.classify, shape)
        # load end
        #print("classez2", myobj.classes)
        print(model)
        self.printgpus()
        classifier = model
        if hasattr(ds, 'train'):
            (accuracy_score, loss, train_accuracy_score, train_loss, val_accuracy, val_loss) = self.do_learntestinner(myobj, config, classifier, ds.train, ds.traincat, ds.test, ds.testcat, meta.classify, val, valcat)
            myobj.classifyarray = train
            (intlist, problist) = self.do_classifyinner(myobj, model, config, meta.classify)
        else:
            (accuracy_score, loss, train_accuracy_score, train_loss, val_accuracy, val_loss) = classifier.train(ds)
            #(accuracy_score, loss, train_accuracy_score) = (0, 0, 0)
        global dictclass
        #dictclass[str(myobj.modelInt) + myobj.period + myobj.modelname] = classifier
        #global dicteval
        #dicteval[myobj.modelname] = float(accuracy_score)
        #print("seteval" + str(myobj.modelname))
        
        if not self.wantDynamic(myobj) and self.wantLearn(myobj):
            if model.localsave():
                print("Saving")
                model.save(self.getfullpath(myobj))

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
        queue.put({"accuracy": accuracy_score, "trainaccuracy": train_accuracy_score, "loss": loss, 'train_loss' : train_loss, 'valaccuracy' : val_accuracy, 'val_loss' : val_loss, "classify" : meta.classify, "gpu" : self.hasgpu() })
        #return Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json')

    def get_file(self, request):
        """Extract uploaded file paths from a Flask/Werkzeug request.

        Looks for files under keys 'file' and 'file2' and saves them to /tmp
        using a secure filename. Returns a tuple (filename, filename2) where
        values may be None if the corresponding file key is missing.
        """
        filename = self.getFilename(request, 'file')
        filename2 = self.getFilename(request, 'file2')
        return (filename, filename2)

    def getFilename(self, request, key):
        """Save an uploaded file from a request to /tmp and return the path.

        Args:
            request: Werkzeug/Flask request containing `files` mapping.
            key: the key in request.files to extract.

        Returns:
            str or None: the saved filepath in /tmp or None when missing.
        """
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

    def do_dataset_gen(self, queue, myjson, filenames):
        """Handle dataset generation and training for generative models.

        Supports VAE, conditional GAN, DCGAN and neural style transfer model
        flows. Loads datasets (using `mydatasets.getdatasetgen`), compiles and
        trains the generative model, optionally generates output files, and
        returns metrics via the provided queue.

        Args:
            queue: multiprocessing.Queue for results.
            myjson: JSON string representing LearnTest-like configuration.
            filenames: list/tuple of uploaded filenames (used by style transfer).
        """
        dt = datetime.now()
        timestamp = dt.timestamp()
        #print("1111")
        #print(request.get_data(as_text=True))
        #print("2222")

        filename = filenames[0]
        filename2 = filenames[1]

        print("Filename", filename, filename2)

        #print("rrr0", request.form)
        #print("rrr", request.files['json'])
        myobj = json.loads(myjson, object_hook=lt.LearnTest)
        (config, modelname) = self.getModel(myobj)
        Model = importlib.import_module('model.' + modelname)
        if modelname == 'vae':
            (dataset, size, classes, classify, from_logits, encoder, decoder) = mydatasets.getdatasetgen(myobj, config, self)
            model = Model.Model(myobj, config, classify, encoder, decoder)
        if modelname == 'conditional_gan' or modelname == 'dcgan':
            (dataset, size, classes, classify, from_logits) = mydatasets.getdatasetgen(myobj, config, self)
            if hasattr(config, 'take'):
                dataset = dataset.take(config.take)

            #myobj.size = size # TODO
            myobj.classes = classes

            model = Model.Model(myobj, config)
        if modelname == 'neural_style_transfer':
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
        if modelname == 'vae':
            model.compile(optimizer=keras.optimizers.Adam())
            model.fit(dataset, epochs=config.steps, batch_size=128)
            gan = model
        if modelname == 'conditional_gan' or modelname == 'dcgan':
            gan = model
            gan.compile(
                d_optimizer=tf.keras.optimizers.Adam(learning_rate=config.lr),
                g_optimizer=tf.keras.optimizers.Adam(learning_rate=config.lr),
                loss_fn=tf.keras.losses.BinaryCrossentropy(from_logits=from_logits),
            )

            gan.fit(dataset, epochs=config.steps, callbacks = [ gan.getcallback() ])
        if modelname == 'neural_style_transfer':
            gan = model

        #if not self.wantDynamic(myobj) and self.wantLearn(myobj):
        #    if model.localsave():
        #        print("Saving")
        #        model.save(self.getpath(myobj) + myobj.filename + ".keras")

        if hasattr(myobj, 'generate') and myobj.generate:
            files = gan.generate()
            print("files", files)

        if not modelname == 'neural_style_transfer':
            print("met", gan.metrics)
            print("met", gan.metrics[0].result(), gan.metrics[1].result())
            t = max(gan.metrics[0].result(), gan.metrics[1].result())
            loss = tf.get_static_value(t)
        else:
            loss = gan.metrics
        print("met", loss)

        classifier = model

        #classifier.tidy()
        del classifier
        dt = datetime.now()
        print("millis ", (dt.timestamp() - timestamp) * 1000)
        queue.put(
            {"accuracy": None, "trainaccuracy": None, "loss": loss, "classify": False,
             "gpu": self.hasgpu(), "files" : files } )
        # return Response(json.dumps({"accuracy": float(accuracy_score)}), mimetype='application/json')

    def do_imgclassify(self, queue, myjson, filenames):
        """Classify an uploaded image using an image model.

        Loads dataset metadata to determine expected input size, preprocesses
        the uploaded image, builds/loads the model, and runs classification
        returning results via `queue`.

        Args:
            queue: multiprocessing.Queue for result dict.
            myjson: JSON string describing the model/dataset.
            filenames: list/tuple where the first element is the image path.
        """
        print("eager", tf.executing_eagerly())
        #tf.logging.set_verbosity(tf.logging.FATAL)
        dt = datetime.now()
        timestamp = dt.timestamp()
        #myobj = json.loads(request, object_hook=lt.LearnTest)
        filename = filenames[0]
        print("Filename", filename)
        myobj = json.loads(myjson, object_hook=lt.LearnTest)

        (config, modelname) = self.getModel(myobj)
        print("Cf", config, modelname)
        Model = importlib.import_module('model.' + modelname)
        (ds, meta) = mydatasets.getdataset(myobj, config, self)

        img = self.preprocess_image(filename, meta.origsize)

        train = np.array(ds.train)
        myobj.trainingarray = train
        myobj.trainingcatarray = ds.traincat
        #myobj.size = meta.size # TODO
        myobj.classes = meta.classes
        (train, traincat, test, testcat, shape, val, valcat) = self.gettraintest(myobj, config, meta.classify)
        # print("classez2", myobj.classes)
        model = Model.Model(myobj, config, meta.classify, shape)
        exists = self.exists(myobj)
        # load model if:
        # exists and not dynamic and wantclassify
        if exists and not self.wantDynamic(myobj) and self.wantClassify(myobj):
            if Model.Model.localsave():
                # dummy variable to allow saver
                model = Model.Model(myobj, config, meta.classify, shape)
                print("Restoring")
                model.model = tf.keras.models.load_model(self.getfullpath(myobj))
                print("Restoring done")
            else:
                model = Model.Model(myobj, config, meta.classify, shape)
        else:
            model = Model.Model(myobj, config, meta.classify, shape)
        # load end
        # print("classez2", myobj.classes)
        print(model)
        self.printgpus()
        classifier = model
        #print("rrr0", request.form)
        #print("rrr", request.files['json'])
        if config.name == 'mlp':
            print(type(img))
            print(img.shape)
            img = tf.reshape(img, (img.shape[0], img.shape[1] * img.shape[2], img.shape[3]))
            print(type(img))
            print(img.shape)
        myobj.classifyarray = img
        (intlist, problist) = self.do_classifyinner(myobj, model, config, True)
        print("list", intlist, problist)
        classifier.tidy()
        del classifier
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        queue.put({"classifycatarray": intlist, "classifyprobarray": problist, "accuracy": None, "trainaccuracy": None, "loss": None, "classify" : meta.classify, "gpu" : self.hasgpu() })

    def do_gpt(self, queue, myjson, cachedata):
        """Run/generate text from a GPT-style model.

        Loads or reuses an existing model (from cachedata), prepares datasets as
        needed, optionally trains and/or generates text, and returns the
        generated text and metrics via the given queue.

        Args:
            queue: multiprocessing.Queue for returning results.
            myjson: JSON string describing the GPT job.
            cachedata: optional cached model/data to reuse.

        Returns:
            model: the instantiated model object (useful when caller caches it).
        """
        dt = datetime.now()
        timestamp = dt.timestamp()
        print("my", myjson)
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
                    model.model = tf.keras.models.load_model(self.getfullpath(myobj))
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

        loss = None
        if self.wantLearn(myobj):
            loss = model.metrics

        if not self.wantDynamic(myobj) and self.wantLearn(myobj):
            print(model.model.summary())
            if model.dataset.train_ds is not None:
                model.fit()
            if model.localsave():
                print("Saving")
                model.save(self.getfullpath(myobj))

        #classifier.tidy()
        del classifier
        accuracy_score = None
        train_accuracy_score = None
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

    def preprocess_image(self, image_path, size):
        """Load and preprocess an image file for image-based models.

        Loads the image from `image_path`, resizes it to the expected `size`,
        converts it to a grayscale tensor and returns a TensorFlow tensor
        suitable for model input.

        Args:
            image_path: filesystem path to the image file.
            size: tuple/list describing expected image dimensions (width,height) or shape used by the model.

        Returns:
            tf.Tensor: preprocessed image tensor.
        """
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
        """Return filesystem path for saving/loading model files.

        Uses `myobj.path` when present, otherwise defaults to '/tmp/'. Ensures
        the returned string ends with a trailing slash.
        """
        if hasattr(myobj, 'path') and not myobj.path is None:
            return myobj.path + '/'
        return '/tmp/'

    def getfullpath(self, myobj):
        """Return full filesystem path for a saved model including filename.

        Appends the `.keras` extension to `myobj.filename` and prefixes with the
        path returned by `getpath`.
        """
        return self.getpath(myobj) + myobj.filename + ".keras"

    def do_filename(self, queue, request):
        """Extract filename info and return existence boolean via queue.

        Reads a LearnTest object from the request body, checks whether the
        corresponding saved model exists, and places a Response with a JSON
        boolean `exists` into the provided queue.
        """
        myobj = json.loads(request.get_data(as_text=True), object_hook=lt.LearnTest)
        exists = self.exists(myobj)
        queue.put(Response(json.dumps({"exists": exists}), mimetype='application/json'))

    def printgpus(self):
        """Print available GPUs detected by TensorFlow.

        Useful for debugging and logging environment information.
        """
        #from tensorflow.python.client import device_lib
        #print(device_lib.list_local_devices())
        from keras import backend as K
        gpus = tf.config.list_physical_devices('GPU') 
        print(type(gpus))
        print("GPUs", gpus)

    def hasgpu(self):
        """Return True when at least one GPU device is available to TensorFlow."""
        physical_devices = tf.config.list_physical_devices('GPU')
        return len(physical_devices) > 0
