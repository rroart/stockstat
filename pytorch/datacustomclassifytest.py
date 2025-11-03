import unittest
import numpy as np

import config
import datacli as cli
import mydatasetscommon
import mydatasetscustom

steps = 1

class MyTestCase(unittest.TestCase):
    def test_something(self):
        #return
        train_x, train_y = mydatasetscustom.common()

        testlist = [ config.PYTORCHLSTM ]
        testlist = [ config.PYTORCHMLP ]
        testlist = [ config.PYTORCHRNN ]
        testlist = [ config.PYTORCHCNN, config.PYTORCHCNN2 ]
        #testlist = [ config.PYTORCHCNN2 ]
        size = 4
        for test in testlist:
            print("Doing", test)
            for i in range(1, 4):
                for j in range(1, 3):
                    for k in [1, 2, 3, 4]:
                        for l in [1, 2, 3, 4]:
                            override = { 'convlayers' : i, 'layers' : j, 'maxpool' : k, 'kernelsize' : l }
                            result = cli.learntestclassifynotest(None, cf = test, train_x = train_x, train_y = train_y, steps = steps, size = size, classes = 4, zero = False, override = override)
            print(result)
            #self.assertIsNotNone(result['accuracy'], "Accuracy")
            #self.assertIsNotNone(result['classifycatarray'], "Classify")

            #result = cli.learntest(cf = test, train_x = train_x, train_y = train_y, test_x = test_x, test_y = test_y, steps = steps, size = size, classes = 3)
            #print(result)
            #self.assertIsNotNone(result['accuracy'], "Accuracy")

            #result = cli.classify(IRISTEST, cf=test, train_x = IRISTEST, train_y = IRISCLASS, size=size, classes = 3)
            #print(result)
            #self.assertIsNotNone(result['classifycatarray'], "Classify")


    def test_iris(self):
        binary = True
        #binary = False
        steps = 10

        train_x, train_y, test_x, test_y, classes = mydatasetscommon.iriscommon(binary)
        #train_x = np.array(train_x).reshape(120, 2, 2).tolist()  # train_x.values.tolist()
        #test_x = np.array(test_x).reshape(30, 2, 2).tolist()  # test_x.values.tolist()

        testlist = [ config.PYTORCHRNN, config.PYTORCHCNN, config.PYTORCHLSTM, config.PYTORCHGRU ]
        testlist = [ config.PYTORCHMLP ]
        size = (2, 2)
        size = 4
        for test in testlist:
            print("Doing", test)
            result = cli.learntestclassify(test_x, cf = test, train_x = train_x, train_y = train_y, test_x = test_x, test_y = test_y, steps = steps, size = size, classes = classes)
            print(result)
            #self.assertIsNotNone(result['accuracy'], "Accuracy")
            #self.assertIsNotNone(result['classifycatarray'], "Classify")

            result = cli.learntest(cf = test, train_x = train_x, train_y = train_y, test_x = test_x, test_y = test_y, steps = steps, size = size, classes = classes)
            print(result)
            #self.assertIsNotNone(result['accuracy'], "Accuracy")

            result = cli.classify(test_x, cf=test, train_x = train_x, train_y = train_y, size=size, classes = classes)
            print(result)
            #self.assertIsNotNone(result['classifycatarray'], "Classify")


        # here

# todo cnn2

if __name__ == '__main__':
    unittest.main()
