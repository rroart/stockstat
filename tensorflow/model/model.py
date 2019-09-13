import tensorflow as tf
import numpy as np

class MyModel():
    def __init__(self, config, name):
        self.config = config

    def train(self, train, traincat):
        # this calls call
        #self.fit(train, traincat, epochs = self.config.steps)
        #print("train")
        self.model.fit(train, traincat, epochs = self.config.steps, verbose = 0)

    def evaluate(self, array, cat):
        #return super(MyModel, self).evaluate(array, cat)
        return self.model.evaluate(array, cat)
    
    def predict(self, array):
        intlist = []
        problist = []
        #print(array)
        #predictions = super(MyModel, self).predict(array)
        predictions = self.model.predict(array)
        #print(type(predictions))
        #print("predictions")
        #print(predictions)
        for prediction in predictions:
            max = np.argmax(prediction)
            #print(prediction[max])
            intlist.append(int(max))
            problist.append(float(prediction[max]))
        #del predictions    
        return intlist, problist
        
    def tidy(self):
        return None