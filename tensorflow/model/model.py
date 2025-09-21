import tensorflow as tf
import numpy as np

class MyModel():
    def __init__(self, config, classify, name):
        self.config = config
        self.classify = classify
        
    def train(self, train, traincat, x_val = None, y_val = None):
        # this calls call
        #self.fit(train, traincat, epochs = self.config.steps)
        print("train")
        print(train.shape)
        print(traincat.shape)
        #dataset = tf.data.Dataset.from_tensor_slices((train, traincat))
        #self.model.fit(dataset, epochs = self.config.steps, verbose = 0)
        if x_val is None or y_val is None:
            x_val = train
            y_val = traincat
        return self.model.fit(train, traincat, epochs = self.config.steps, verbose = 0, validation_data=(x_val, y_val),)

    def evaluate(self, array, cat):
        #return super(MyModel, self).evaluate(array, cat)
        return self.model.evaluate(array, cat)
    
    def predict(self, array):
        intlist = []
        problist = []
        #print(array)
        #print(type(array))
        #predictions = super(MyModel, self).predict(array)
        predictions = self.model.predict(array)
        #print(type(predictions))
        #print("predictions")
        #print(predictions)
        if self.classify:
            for prediction in predictions:
                #print(prediction)
                max = np.argmax(prediction)
                #print(prediction[max])
                intlist.append(int(max))
                problist.append(float(prediction[max]))
        else:
            intlist = predictions.flatten().tolist()
            problist = [ None ] * len(intlist)
        #del predictions    
        return intlist, problist
        
    def tidy(self):
        return None

    @staticmethod
    def localsave():
        return True

    def save(self, filename):
        self.model.save(filename)
