import unittest

import config
import datasetcli as cli

class MyTestCase(unittest.TestCase):
    def test_something(self):
        testlist = [ config.TENSORFLOWDNN, config.TENSORFLOWLIC, config.TENSORFLOWMLP, config.TENSORFLOWRNN, config.TENSORFLOWCNN, config.TENSORFLOWLSTM, config.TENSORFLOWGRU, config.TENSORFLOWCNN2 ]
        testlist = [ config.TENSORFLOWCNN2 ]
        for test in testlist:
          dslist = [ 'mnist', 'cifar10' ]
          for ds in dslist:
            result = cli.learn(ds = ds, cf = test, take = 40, steps = 1)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
            # toto classify
        # here

if __name__ == '__main__':
    unittest.main()
