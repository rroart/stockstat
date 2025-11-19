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

        testlist = [ config.TENSORFLOWDNN, config.TENSORFLOWLIC, config.TENSORFLOWMLP ]
        testlist = [ config.TENSORFLOWLSTM ]
        testlist = [ config.TENSORFLOWRNN ]
        testlist = [ config.TENSORFLOWMLP ]
        testlist = [ config.TENSORFLOWCNN, config.TENSORFLOWCNN2 ]
        testlist = [ config.TENSORFLOWCNN2, config.TENSORFLOWCNN ]
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
            self.assertIsNotNone(result['accuracy'], "Accuracy")
            #self.assertIsNotNone(result['classifycatarray'], "Classify")

            #result = cli.learntest(cf = test, train_x = train_x, train_y = train_y, test_x = test_x, test_y = test_y, steps = steps, size = size, classes = 3)
            #print(result)
            #self.assertIsNotNone(result['accuracy'], "Accuracy")

            #result = cli.classify(IRISTEST, cf=test, train_x = IRISTEST, train_y = IRISCLASS, size=size, classes = 3)
            #print(result)
            #self.assertIsNotNone(result['classifycatarray'], "Classify")


    def test_iris(self):
      binary = False
      binary = True
      steps = 10
      testlist = [config.TENSORFLOWRNN, config.TENSORFLOWCNN, config.TENSORFLOWLSTM, config.TENSORFLOWGRU]
      testlist = [config.TENSORFLOWMLP]
      size = (2, 2)
      size = 4
      override = None
      #override = { "lastactivation" : None }
      #override = { "lr" : 0.1, "normalize" : True, "batchnormalize" : False, "inputdropout" : 0, "dropout" : 0, "hidden" : 16, "batchsize" : 16, "layers" : 1, "lastactivation" : None }
      #override = { "lr" : 0.01, steps : 100, "normalize" : False, "batchnormalize" : False, "inputdropout" : 0, "dropout" : 0, "hidden" : 16, "batchsize" : 16, "layers" : 1, "lastactivation" : None }
      override = { "steps" : 30, "lr" : 0.1, "normalize" : True, "batchnormalize" : False, "inputdropout" : 0, "dropout" : 0, "hidden" : 16, "batchsize" : 16, "layers" : 1 }
      override = { "steps" : 30, "lr" : 0.1, "normalize" : False, "batchnormalize" : False, "inputdropout" : 0, "dropout" : 0, "hidden" : 16, "batchsize" : 16, "layers" : 1 }
      for test in testlist:
          for binary in [ True, False ]:
          #for binary in []: #[ True, False ]:

            classes = 2
            train_x, train_y, test_x, test_y, classes = mydatasetscommon.iriscommon(classes)
            #train_x, train_y, test_x, test_y, classes = mydatasetscommon.mnistcommon(binary)
            #train_x = np.array(train_x).reshape(120, 2, 2).tolist()  # train_x.values.tolist()
            #test_x = np.array(test_x).reshape(30, 2, 2).tolist()  # test_x.values.tolist()

            self.single_run(binary, classes, size, steps, test, test_x, test_y, train_x, train_y, override)

          binary = False
          classes = 3
          train_x, train_y, test_x, test_y, classes = mydatasetscommon.iriscommon()
          self.single_run(binary, classes, size, steps, test, test_x, test_y, train_x, train_y, override)
      # here

    def single_run(self, binary: bool, classes: int, size: int, steps: int, test: str, test_x, test_y, train_x, train_y,
                   override):
        print("Doing", test)
        result = cli.learntestclassify(test_x, cf=test, train_x=train_x, train_y=train_y, test_x=test_x, test_y=test_y,
                                       steps=steps, size=size, classes=classes, binary=binary, override = override)
        print(result)
        self.assertIsNotNone(result['accuracy'], "Accuracy")
        self.assertIsNotNone(result['classifycatarray'], "Classify")

        result = cli.learntest(cf=test, train_x=train_x, train_y=train_y, test_x=test_x, test_y=test_y, steps=steps,
                               size=size, classes=classes, binary=binary, override = override)
        print(result)
        self.assertIsNotNone(result['accuracy'], "Accuracy")

        result = cli.classify(test_x, cf=test, train_x=None, train_y=None, size=size, classes=classes,
                              binary=binary, override = override)
        print(result)
        self.assertIsNotNone(result['classifycatarray'], "Classify")

    def test_mnist(self):
      binary = False
      binary = True
      classes = 2
      steps = 10
      testlist = [config.TENSORFLOWRNN, config.TENSORFLOWCNN, config.TENSORFLOWLSTM, config.TENSORFLOWGRU]
      testlist = [config.TENSORFLOWMLP]
      size = (2, 2)
      size = 4
      override = { }
      for test in testlist:
          for binary in [ True, False ]:

            #train_x, train_y, test_x, test_y, classes = mydatasetscommon.iriscommon(binary)
            train_x, train_y, test_x, test_y, classes = mydatasetscommon.mnistcommon(classes)
            #train_x = np.array(train_x).reshape(120, 2, 2).tolist()  # train_x.values.tolist()
            #test_x = np.array(test_x).reshape(30, 2, 2).tolist()  # test_x.values.tolist()

            self.single_run(binary, classes, size, steps, test, test_x, test_y, train_x, train_y, override)

          binary = False
          classes = 10
          train_x, train_y, test_x, test_y, _ = mydatasetscommon.mnistcommon()
          self.single_run(binary, classes, size, steps, test, test_x, test_y, train_x, train_y, override)

# todo cnn2

if __name__ == '__main__':
    unittest.main()
