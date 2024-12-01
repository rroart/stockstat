import unittest
import numpy as np

import config
import datacli as cli
import mydatasetscommon

IRISTEST=[[5.9,3.0,4.2,1.5]]
IRISTEST2=[[[5.9,3.0],[4.2,1.5]]]
#IRISTEST2=[[5.9,3.0],[4.2,1.5]]
IRISCLASS=[[1]]

# TODO trainingarray should not be needed when classify only

class MyTestCase(unittest.TestCase):
    def test_something(self):
        return
        train_x, train_y, test_x, test_y = mydatasetcommon.iriscommon()

        testlist = [ config.TENSORFLOWDNN, config.TENSORFLOWLIC, config.TENSORFLOWMLP ]
        size = 4
        for test in testlist:
            print("Doing", test)
            result = cli.learntestclassify(IRISTEST, cf = test, train_x = train_x, train_y = train_y, test_x = test_x, test_y = test_y, steps = 1, size = size, classes = 3)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")
            self.assertIsNotNone(result['classifycatarray'], "Classify")

            result = cli.learntest(cf = test, train_x = train_x, train_y = train_y, test_x = test_x, test_y = test_y, steps = 1, size = size, classes = 3)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")

            result = cli.classify(IRISTEST, cf=test, train_x = IRISTEST, train_y = IRISCLASS, size=size, classes = 3)
            print(result)
            self.assertIsNotNone(result['classifycatarray'], "Classify")


    def test_something2(self):
        #return
        train_x, train_y, test_x, test_y = mydatasetcommon.iriscommon()
        train_x = np.array(train_x).reshape(120, 2, 2).tolist()  # train_x.values.tolist()
        test_x = np.array(test_x).reshape(30, 2, 2).tolist()  # test_x.values.tolist()

        testlist = [ config.TENSORFLOWRNN, config.TENSORFLOWCNN, config.TENSORFLOWLSTM, config.TENSORFLOWGRU ]
        size = (2, 2)
        for test in testlist:
            print("Doing", test)
            result = cli.learntestclassify(IRISTEST2, cf = test, train_x = train_x, train_y = train_y, test_x = test_x, test_y = test_y, steps = 1, size = size, classes = 3)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")
            self.assertIsNotNone(result['classifycatarray'], "Classify")

            result = cli.learntest(cf = test, train_x = train_x, train_y = train_y, test_x = test_x, test_y = test_y, steps = 1, size = size, classes = 3)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")

            result = cli.classify(IRISTEST2, cf=test, train_x = IRISTEST2, train_y = IRISCLASS, size=size, classes = 3)
            print(result)
            self.assertIsNotNone(result['classifycatarray'], "Classify")


        # here

# todo cnn2

if __name__ == '__main__':
    unittest.main()
