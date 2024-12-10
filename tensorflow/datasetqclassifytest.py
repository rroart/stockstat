import unittest

import config
import datasetcli as cli

class MyTestCase(unittest.TestCase):
    def test_something(self):
        testlist = [ config.TENSORFLOWQNN, config.TENSORFLOWQCNN, config.TENSORFLOWPQK ]
        #testlist = [config.TENSORFLOWQNN]
        testlist = [config.TENSORFLOWQCNN]
        #testlist = [config.TENSORFLOWPQK]
        testlist = [config.TENSORFLOWQNN]

        for test in testlist:
            #result = cli.learn(ds = 'mnist', cf = test, take = 40, steps = 1, q = True)
            #result = cli.learn(ds = 'fashion_mnist', cf = test, take = 40, steps = 1, q = True)
            #result = cli.learn(ds = 'iris2', cf = test, take = 40, steps = 1, q = True)
            result = cli.learn(ds='iris', cf=test, take=40, steps=1, q=True)

            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
        # here

if __name__ == '__main__':
    unittest.main()
