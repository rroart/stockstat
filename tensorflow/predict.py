import learntestpredict as ltp
import device

import os
import time
import warnings
import numpy as np
from numpy import newaxis
from keras.layers.core import Dense, Activation, Dropout
from keras.layers.recurrent import LSTM
from keras.models import Sequential
from keras import backend
from sklearn.preprocessing import MinMaxScaler
from sklearn.metrics import mean_squared_error

import json
from nameko.web.handlers import http
from datetime import datetime
from werkzeug.wrappers import Response

def create_dataset2(dataset, xsize, look_back=1):
    dataX, dataY = [], []
    for j in range(xsize):
        train = dataset[j]
        print(train)
        trainX, trainY = create_dataset(train, look_back)
        print(trainX.shape)
        trainX = np.reshape(trainX, (trainX.shape[0], 1, trainX.shape[1]))
        print(trainX.shape)
        dataX.append(trainX)
        dataY.append(trainY)
    return (dataX, dataY)

def create_dataset(dataset, look_back=1):
    dataX, dataY = [], []
    for i in range(len(dataset)-look_back-1):
        a = dataset[i:(i+look_back), 0]
        dataX.append(a)
        dataY.append(dataset[i + look_back, 0])
    return np.array(dataX), np.array(dataY)

def normalise_windows(window_data):
    normalised_data = []
    for window in window_data:
        normalised_window = [((float(p) / float(window[0])) - 1) for p in window]
        normalised_data.append(normalised_window)
    return normalised_data

#def normme(listlist):
#    newlist = []
#    for l in listlist:
#        normalised_window = normalize_windows(l)
#        normalised_data.append(normalised_window)
#    return newlist

def old_build_model(layers):
    model = Sequential()
    model.add(LSTM(input_dim = layers[0], output_dim = layers[1], return_sequences = True))
    model.add(Dropout(0.2))
    model.add(LSTM(layers[2], return_sequences = False))
    model.add(Dropout(0.2))
    model.add(Dense(output_dim = layers[3]))
    model.add(Activation("linear"))
    start = time.time()
    model.compile(loss="mse", optimizer="rmsprop", metrics=['accuracy'])
    print("> Compilation Time : ", time.time() - start)
    return model

def build_model(look_back):
    model = Sequential()
    model.add(LSTM(4, input_shape=(1, look_back)))
    model.add(Dense(1))
    model.compile(loss='mean_squared_error', optimizer='adam', metrics=['accuracy'])
    return model

def build_model2(look_back):
    model = Sequential()
    model.add(LSTM(4, input_shape=(1, look_back)))
    model.add(Dense(1))
    model.compile(loss='mean_squared_error', optimizer='adam', metrics=['accuracy'])
    return model

def predict_point_by_point(model, data):
    #Predict each timestep given the last sequence of true data, in effect only predicting 1 step ahead each time
    predicted = model.predict(data)
    predicted = np.reshape(predicted, (predicted.size,))
    return predicted

def predict_sequence_full(model, data, window_size, horizon):
    #Shift the window by 1 new prediction each time, re-run predictions on new window
    curr_frame = data
    predicted = []
    print("here0")
    print(len(data))
    print(data)
    print(data.shape)
    print(type(data))
    for i in range(horizon):
        print (curr_frame)
        predme = curr_frame
        #predme = curr_frame[newaxis,:,:]
        print(type(predme))
        print(predme.shape)
        print(predme)
        pred = model.predict(predme)[0][0]
        print (pred)
        predicted.append(float(pred))
        print("currf")
        curr_frame=predme[0][0]
        print(curr_frame)
        curr_frame = curr_frame[1:]
        print(curr_frame)
        curr_frame = np.insert(curr_frame, [window_size-1], pred, axis=0)
        print(curr_frame)
        curr_frame=curr_frame[np.newaxis, np.newaxis, :]
        print(curr_frame.shape)
    print( predicted)
    return predicted

class Predict:
    def do_learntestone(self, array, myobj):
        array = np.array(array)
        look_back = myobj.windowsize
        epochs = myobj.epochs
        #look_back = 10
        #slide = myobj.slide
        #print(slide)
        #print(type(slides))
        #array = np.array(predict)
        print("herex",array.shape)
        predictme = array[-look_back:]
        print("here")
        #print(predictme)
        #print(array)
        print(array.shape)
        print(predictme.shape)
        dataset = array

        dataset = dataset.astype('float32')
        dataset = dataset[:, np.newaxis]
        print(type(dataset))
        print(dataset.shape)
        predictme = predictme.astype('float32')
        predictme = predictme[:, np.newaxis]
        
        # normalize the dataset
        print("dataset ", dataset)
        scaler = MinMaxScaler(feature_range=(0, 1))
        dataset = scaler.fit_transform(dataset)
        print("dataset0 ", dataset)
        print(dataset.shape)
        print(predictme.shape)
        print("preme")
        print(predictme)
        predictme = scaler.fit_transform(predictme)
        print(predictme)
        
        # split into train and test sets
        #train_size = int(len(dataset) * 0.67)
        train_size = int(len(dataset) * 1)
        #test_size = len(dataset) - train_size
        train, test = dataset[0:train_size,:], dataset[train_size:len(dataset),:]
        # reshape into X=t and Y=t+1
        trainX, trainY = create_dataset(train, look_back)
        #testX, testY = create_dataset(test, look_back)
        # reshape input to be [samples, time steps, features]
        print(trainX.shape)
        print(predictme.shape)
        trainX = np.reshape(trainX, (trainX.shape[0], 1, trainX.shape[1]))
        predictme = np.reshape(predictme, (predictme.shape[1], 1, predictme.shape[0]))
        #predictme = predictme[np.newaxis, :]
        print("reshaped")
        print(trainX.shape)
        print(predictme.shape)
        print(predictme)
        #testX = np.reshape(testX, (testX.shape[0], 1, testX.shape[1]))
        # create and fit the LSTM network
        model = build_model(look_back)

        print("tx ", trainX)
        print("tx ", trainX.shape)
        model.fit(trainX, trainY, epochs=epochs, batch_size=1, verbose=2)
        # make predictions
        #score = model.evaluate(testX, testY, batch_size=1)
        #print("Score")
        #print(score)
        trainPredict = model.predict(trainX)
        print(trainPredict)
        print(trainPredict.shape)
        horizon = myobj.horizon
        predicted = predict_sequence_full(model, predictme, look_back, horizon)
        predicted = np.array(predicted)
        predicted = predicted.astype('float32')
        predicted = predicted[:, np.newaxis] 
        print(predicted)
        predicted = scaler.inverse_transform(predicted)
        print(predicted)
        print(predicted.shape)
        predicted=predicted[:, 0]
        predicted = predicted.astype('float').tolist()
        print(predicted)
        #testPredict = model.predict(testX)
        #print(testPredict.shape)
        #print(type(testPredict))
        #print(testPredict)
        # invert predictions
        #trainPredict = scaler.inverse_transform(trainPredict)
        #trainY = scaler.inverse_transform([trainY])

        #trainScore = math.sqrt(mean_squared_error(trainY[0], trainPredict[:,0]))
        #print('Train Score: %.2f RMSE' % (trainScore))

        #(lenrow, lencol) = array.shape
        #half = round(lenrow / 2)
        #trainfull = array[:half, :]
        #testfull = array[half:, :]
        #if len(trainfull) == 1:
        #    trainfull = array
        #    testfull = array
        #train = trainfull[:, :-1]    
        #traincat = trainfull[:, -1]    
        #test = testfull[:, :-1]    
        #testcat = testfull[:, -1]    

        #train = np.reshape(train, (train.shape[0], train.shape[1], 1))
        #test = np.reshape(test, (test.shape[0], test.shape[1], 1))

        #print(predictme.shape)
        #predictme = np.reshape(predictme, (predictme.shape[0], predictme.shape[1], 1))

#        train = tf.constant(train)
#        test = tf.constant(test)
        
        #epochs = 1
#        epochs = 500
        #seq_len = lencol - 1
        #window_size = seq_len
        #print("windowsize")
        #print(window_size)
        #model = build_model([1, window_size, 2 * window_size, 1])
        #model.fit(
        #        train,
        #        traincat,
        #        batch_size=512,
        #        epochs=epochs,
        #        validation_split=0.05)

        #score = model.evaluate(test, testcat, batch_size=512)
        #, show_accuracy=True)
        #print ('LSTM test accuracy:', score)
        #print ('LSTM test accuracy:', score[0])
        #print ('LSTM test accuracy:', score[1])
        
        #print("here")
        #print(predictme.shape)
        #print(type(predictme))
        #predicted = predict_sequence_full(model, predictme, 10, myobj.horizon)
#        predicted2 = predict_point_by_point(model, test)
#        model.layers[0].reset_states()
#        model.reset_states()
        print(predicted)
        return predicted

    def do_learntest(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        print("entering")
        print(request.get_data(as_text=True))
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=ltp.LearnTestPredict)
        print(myobj.array)
        print(type(myobj.array))
        array = myobj.array
        predicted = self.do_learntestone(array, myobj)
#        print(predicted2)
        backend.clear_session()
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        print("leaving")
        return Response(json.dumps({"predicted": predicted}), mimetype='application/json')

    def do_learntestlist(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        print("entering")
        print(request.get_data(as_text=True))
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=ltp.LearnTestPredict)
        print(myobj.arraylist)
        print(type(myobj.arraylist))
        predictedlist = []
        for i in range(len(myobj.arraylist)):
            array = myobj.arraylist[i]
            predicted = self.do_learntestone(array, myobj)
            predictedlist.append(predicted)
        backend.clear_session()
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        print(predictedlist)
#        print(predicted2)
        print("leaving")
        return Response(json.dumps({"predictedlist": predictedlist}), mimetype='application/json')

    def do_learntest2(self, request):
        dt = datetime.now()
        timestamp = dt.timestamp()
        print("entering")
        print(request.get_data(as_text=True))
        #print(request.get_data(as_text=True))
        myobj = json.loads(request.get_data(as_text=True), object_hook=ltp.LearnTestPredict)
        print(myobj.array)
        print(type(myobj.array))
        array = myobj.array
        array = np.array(array)

        look_back = myobj.windowsize
        epochs = myobj.epochs
        #look_back = 10
        #slide = myobj.slide
        #print(slide)
        #print(type(slides))
        #array = np.array(predict)
        print("herex",array.shape)
        predictme = array[:, -look_back:]
        print("here")
        #print(predictme)
        #print(array)
        print(array.shape)
        print(predictme.shape)
        dataset = array

        dataset = dataset.astype('float32')
        (xsize, ysize) = dataset.shape
        newdataset = dataset[:, :, np.newaxis]
        print(type(dataset))
        print(dataset.shape)
        predictme = predictme.astype('float32')
        newpredictme = predictme[:, :, np.newaxis]
        
        # normalize the dataset
        scaler = MinMaxScaler(feature_range=(0, 1))
        for i in range(xsize):
            print("i ", i)
            datasub = dataset[i]
            datasub = datasub[:, np.newaxis]
            print("datasub ", datasub)
            datasub = scaler.fit_transform(datasub)
            print("datasub0 ", datasub)
            newdataset[i] = datasub
        dataset = newdataset    
        print(dataset.shape)
        print(predictme.shape)
        print("preme")
        print(predictme)
        for i in range(xsize):
            print("i ", i)
            datasub = predictme[i]
            datasub = datasub[:, np.newaxis]
            print("datasub ", datasub)
            datasub = scaler.fit_transform(datasub)
            print("datasub0 ", datasub)
            newpredictme[i] = datasub
        predictme = newpredictme
        print(predictme)
        
        # split into train and test sets
        #train_size = int(len(dataset) * 0.67)
        train_size = int(len(dataset) * 1)
        #test_size = len(dataset) - train_size
        print("dshape ", dataset.shape)
        train, test = dataset[0:train_size, :], dataset[train_size:len(dataset), :]
        #for i in range(xsize):
        # reshape into X=t and Y=t+1
        trainX, trainY = create_dataset2(train, xsize, look_back)
        #testX, testY = create_dataset(test, look_back)
        # reshape input to be [samples, time steps, features]
        print(predictme.shape)
        predictme = np.reshape(predictme, (predictme.shape[1], 1, predictme.shape[0]))
        #predictme = predictme[np.newaxis, :]
        print("reshaped")
        print(predictme.shape)
        print(predictme)
        #testX = np.reshape(testX, (testX.shape[0], 1, testX.shape[1]))
        # create and fit the LSTM network
        model = build_model2(look_back)

        print(type(trainX))
        print(type(trainY))
        print("tx ", trainX)
        print("tx ", trainX[0].shape)
        model.fit(trainX, trainY, epochs=epochs, batch_size=1, verbose=2)
        # make predictions
        #score = model.evaluate(testX, testY, batch_size=1)
        #print("Score")
        #print(score)
        trainPredict = model.predict(trainX)
        print(trainPredict)
        print(trainPredict.shape)
        horizon = myobj.horizon
        predicted = predict_sequence_full(model, predictme, look_back, horizon)
        predicted = np.array(predicted)
        predicted = predicted.astype('float32')
        predicted = predicted[:, np.newaxis] 
        print(predicted)
        predicted = scaler.inverse_transform(predicted)
        print(predicted)
        print(predicted.shape)
        predicted=predicted[:, 0]
        predicted = predicted.astype('float').tolist()
        print(predicted)
        #testPredict = model.predict(testX)
        #print(testPredict.shape)
        #print(type(testPredict))
        #print(testPredict)
        # invert predictions
        #trainPredict = scaler.inverse_transform(trainPredict)
        #trainY = scaler.inverse_transform([trainY])

        #trainScore = math.sqrt(mean_squared_error(trainY[0], trainPredict[:,0]))
        #print('Train Score: %.2f RMSE' % (trainScore))

        #(lenrow, lencol) = array.shape
        #half = round(lenrow / 2)
        #trainfull = array[:half, :]
        #testfull = array[half:, :]
        #if len(trainfull) == 1:
        #    trainfull = array
        #    testfull = array
        #train = trainfull[:, :-1]    
        #traincat = trainfull[:, -1]    
        #test = testfull[:, :-1]    
        #testcat = testfull[:, -1]    

        #train = np.reshape(train, (train.shape[0], train.shape[1], 1))
        #test = np.reshape(test, (test.shape[0], test.shape[1], 1))

        #print(predictme.shape)
        #predictme = np.reshape(predictme, (predictme.shape[0], predictme.shape[1], 1))

#        train = tf.constant(train)
#        test = tf.constant(test)
        
        #epochs = 1
#        epochs = 500
        #seq_len = lencol - 1
        #window_size = seq_len
        #print("windowsize")
        #print(window_size)
        #model = build_model([1, window_size, 2 * window_size, 1])
        #model.fit(
        #        train,
        #        traincat,
        #        batch_size=512,
        #        epochs=epochs,
        #        validation_split=0.05)

        #score = model.evaluate(test, testcat, batch_size=512)
        #, show_accuracy=True)
        #print ('LSTM test accuracy:', score)
        #print ('LSTM test accuracy:', score[0])
        #print ('LSTM test accuracy:', score[1])
        
        #print("here")
        #print(predictme.shape)
        #print(type(predictme))
        #predicted = predict_sequence_full(model, predictme, 10, myobj.horizon)
#        predicted2 = predict_point_by_point(model, test)
#        model.layers[0].reset_states()
#        model.reset_states()
        backend.clear_session()
        dt = datetime.now()
        print ("millis ", (dt.timestamp() - timestamp)*1000)
        print(predicted)
#        print(predicted2)
        print("leaving")
        return Response(json.dumps({"predicted": predicted}), mimetype='application/json')
    
