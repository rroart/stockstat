import unittest

import config
import datasetcli as cli

class MyTestCase(unittest.TestCase):
    def test_something(self):
        testlist = [ config.PYTORCHMLP, config.PYTORCHRNN, config.PYTORCHLSTM, config.PYTORCHGRU, config.PYTORCHCNN, config.PYTORCHCNN2 ]
        for test in testlist:
            result = cli.learn(ds = 'mnist', cf = test, take = 40, steps = 1)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
        # here

if __name__ == '__main__':
    unittest.main()
