import unittest

import config
import datasetcli as cli
import time

steps = 10
take = None

class MyTestCase(unittest.TestCase):
    def test(self):
        #steps = 1
        testlist = [ config.TENSORFLOWDNN, config.TENSORFLOWLIC, config.TENSORFLOWMLP, config.TENSORFLOWRNN, config.TENSORFLOWCNN, config.TENSORFLOWLSTM, config.TENSORFLOWGRU, config.TENSORFLOWCNN2 ]
        testlist = [ config.TENSORFLOWCNN2 ]
        testlist = [ config.TENSORFLOWRNN ]
        testlist = [ config.TENSORFLOWLSTM ]
        testlist = [ config.TENSORFLOWLSTM, config.TENSORFLOWGRU ]
        testlist = [ config.TENSORFLOWCNN2 ]
        testlist = [ config.TENSORFLOWDNN ]
        testlist = [ config.TENSORFLOWMLP ]
        testlist = [ config.TENSORFLOWDNN, config.TENSORFLOWMLP, config.TENSORFLOWRNN, config.TENSORFLOWCNN, config.TENSORFLOWLSTM, config.TENSORFLOWGRU, config.TENSORFLOWCNN2 ]

        accuracies = {}
        times = {}
        for test in testlist:
          dslist = [ 'mnist', 'cifar10' ]
          dslist = [ 'mnist' ]
          for ds in dslist:
           try:
            curtime = time.time()
            result = cli.learn(ds = 'mnist', cf = test, take = take, steps = steps, override = None)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
            accuracies[(test,ds)] = (result['accuracy'], result['trainaccuracy'])
            times[(test,ds)] = time.time() - curtime
           except Exception:
            import traceback
            traceback.print_exc()
            pass
        print("accuracies", accuracies)
        print("times", times)

if __name__ == '__main__':
    unittest.main()
