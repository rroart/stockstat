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
      steps = 100
      steps = 30
      testlist = [config.PYTORCHRNN, config.PYTORCHCNN, config.PYTORCHLSTM, config.PYTORCHGRU]
      testlist = [config.PYTORCHMLP]
      size = (2, 2)
      size = 4
      override = { "batchsize" : 8 }
      override = { "lr" : 0.01, "batchsize" : 8 }
      override = { "lr" : 0.001, "optimizer" : "adam", "batchnormalize" : False, "inputdropout" : 0, "dropout" : 0, "hidden" : 50, "batchsize" : 128 }
      override = { "lr" : 0.1, "normalize" : False, "batchnormalize" : False, "inputdropout" : 0, "dropout" : 0, "hidden" : 16, "batchsize" : 16, "layers" : 1 }
      override = { "steps" : 30, "lr" : 0.1, "normalize" : True, "batchnormalize" : False, "inputdropout" : 0, "dropout" : 0, "hidden" : 16, "batchsize" : 16, "layers" : 1 }
      # , "lastactivation" : None
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
        #return
        # self.assertIsNotNone(result['accuracy'], "Accuracy")
        # self.assertIsNotNone(result['classifycatarray'], "Classify")

        result = cli.learntest(cf=test, train_x=train_x, train_y=train_y, test_x=test_x, test_y=test_y, steps=steps,
                               size=size, classes=classes, binary=binary, override = override)
        print(result)
        # self.assertIsNotNone(result['accuracy'], "Accuracy")

        result = cli.classify(test_x, cf=test, train_x=train_x, train_y=train_y, size=size, classes=classes,
                              binary=binary, override = override)
        print(result)
        # self.assertIsNotNone(result['classifycatarray'], "Classify")

    def test_mnist(self):
      binary = True
      #binary = False
      classes = 2
      steps = 1000
      testlist = [config.PYTORCHRNN, config.PYTORCHCNN, config.PYTORCHLSTM, config.PYTORCHGRU]
      testlist = [config.PYTORCHMLP]
      size = (2, 2)
      size = 4
      override = { "batchsize" : 8 }
      override = { "lr" : 0.01 }
      override = { }
      override = { "lr" : 0.01 }
      for test in testlist:
          #for binary in []: #[ True, False ]:
          for binary in [ True, False ]:

            #train_x, train_y, test_x, test_y, classes = mydatasetscommon.iriscommon(binary)
            train_x, train_y, test_x, test_y, classes = mydatasetscommon.mnistcommon(classes)
            #train_x = np.array(train_x).reshape(120, 2, 2).tolist()  # train_x.values.tolist()
            #test_x = np.array(test_x).reshape(30, 2, 2).tolist()  # test_x.values.tolist()

            self.single_run(binary, classes, size, steps, test, test_x, test_y, train_x, train_y, override)
            #self.assertIsNotNone(result['classifycatarray'], "Classify")

          binary = False
          classes = 10
          train_x, train_y, test_x, test_y, _ = mydatasetscommon.mnistcommon()
          self.single_run(binary, classes, size, steps, test, test_x, test_y, train_x, train_y, override)

# todo cnn2

if __name__ == '__main__':
    unittest.main()
