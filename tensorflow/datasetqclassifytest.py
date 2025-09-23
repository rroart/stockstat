import unittest

import config
import datasetcli as cli

class MyTestCase(unittest.TestCase):
    def test_something(self):
        testmap = { }
        testmap [ config.TENSORFLOWQNN ] = [ 'mnist', 'fashion_mnist' ]
        testmap [ config.TENSORFLOWQNN ] = [ 'fashion_mnist' ]
        testmap [ config.TENSORFLOWQCNN ] = [ 'mnist', 'fashion_mnist' ]
        testmap [ config.TENSORFLOWPQK ] = [ 'pqk_mnist', 'pqk_fashion_mnist' ]
        testmap [ config.TENSORFLOWQCNN ] = [ 'mnist' ]
        #testmap [ config.TENSORFLOWPQK ] = [ 'mnist' ]
        testmap [ config.TENSORFLOWQNN ] = [ 'mnist',  'fashion_mnist' ]
        testmap [ config.TENSORFLOWQNN ] = [ 'pqk_mnist', 'pqk_fashion_mnist' ]
        testmap [ config.TENSORFLOWQNN ] = [ 'mnist', 'fashion_mnist' ]
        testmap [ config.TENSORFLOWQCNN ] = [ 'mnist',  'fashion_mnist' ]

        testlist = [ config.TENSORFLOWQNN, config.TENSORFLOWQCNN ] #, config.TENSORFLOWPQK ]
        #testlist = [ config.TENSORFLOWQCNN, config.TENSORFLOWPQK ]
        #testlist = [config.TENSORFLOWQNN]
        testlist = [config.TENSORFLOWPQK]
        #testlist = [config.TENSORFLOWQNN]
        testlist = [config.TENSORFLOWQCNN]

        # originally:
        # qnn + mnist
        # qcnn + random
        # pqk + fashion_mnist

        # ok qnn: mnist qcnn: mnist fashion_mnist
        # not ok: qnn: fashion_mnist

        for test in testlist:
            dslist = testmap[test]
            for ds in dslist:
                print("Doing", test, ds)
                result = cli.learn(ds = ds, cf = test, take = 40, steps = 1, q = True)
                #result = cli.learn(ds = 'mnist', cf = test, take = 40, steps = 1, q = True)
                #result = cli.learn(ds = 'fashion_mnist', cf = test, take = 40, steps = 1, q = True)
                #result = cli.learn(ds = 'iris2', cf = test, take = 40, steps = 1, q = True)
                #result = cli.learn(ds='iris', cf=test, take=40, steps=1, q=True)

                print(result)
                self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
        # here

if __name__ == '__main__':
    unittest.main()
