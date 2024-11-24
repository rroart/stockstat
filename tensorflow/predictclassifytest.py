import unittest
import tensorflow as tf
import pandas as pd

import config
import datacli as cli

TRAIN=[[11.0,12.0,13.0,14.0,15.0,16.0,17.0,18.0,19.0,20.0,21.0,22.0,23.0,24.0,25.0,26.0,27.0,28.0,29.0,30.0,31.0,32.0,33.0,34.0,35.0,36.0,37.0,38.0,39.0,40.0,41.0,42.0,43.0,44.0,45.0],[1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,17.0,18.0,19.0,20.0,21.0,22.0,23.0,24.0,25.0,26.0,27.0,28.0,29.0,30.0,31.0,32.0,33.0,34.0,35.0]]
TRAINCAT=[]
CLASSIFY=[[26.0,27.0,28.0,29.0,30.0,31.0,32.0,33.0,34.0,35.0,36.0,37.0,38.0,39.0,40.0,41.0,42.0,43.0,44.0,45.0],[16.0,17.0,18.0,19.0,20.0,21.0,22.0,23.0,24.0,25.0,26.0,27.0,28.0,29.0,30.0,31.0,32.0,33.0,34.0,35.0]]
CLASSIFYCAT=None

class MyTestCase(unittest.TestCase):
    def test_something(self):
        train_x = TRAIN
        train_y = None
        test_x =  None
        test_y = None
        classify = CLASSIFY

        testlist = [ config.TENSORFLOWDNN, config.TENSORFLOWLIC, config.TENSORFLOWMLP,  config.TENSORFLOWRNN, config.TENSORFLOWCNN, config.TENSORFLOWLSTM, config.TENSORFLOWGRU, config.TENSORFLOWLIR ]
        testlist = [ config.TENSORFLOWRNN, config.TENSORFLOWLSTM, config.TENSORFLOWGRU, config.TENSORFLOWLIR ]
        size = 20
        classes = 10
        for test in testlist:
            print("Doing", test)
            result = cli.learntestclassify(cf = test, train_x = train_x, train_y = train_y, test_x = test_x, test_y = test_y, steps = 1, size = size, classes = classes, classify_x = classify, classify = False)
            print(result)
            self.assertIsNotNone(result['loss'], "Loss")  # add assertion
            # self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
       # todo prediect, loss
        # here

if __name__ == '__main__':
    unittest.main()
