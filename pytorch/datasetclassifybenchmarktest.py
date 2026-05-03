import unittest

import config
import datasetcli as cli
import time

steps = 10
take = None

#todo predict

class MyTestCase(unittest.TestCase):
    def test(self):
        steps = 1

        testlist = [ config.PYTORCHRNN ]
        testlist = [ config.PYTORCHGRU ]
        testlist = [ config.PYTORCHMLP ]
        testlist = [ config.PYTORCHCNN, config.PYTORCHCNN2 ]
        testlist = [ config.PYTORCHCNN2 ]
        testlist = [ config.PYTORCHLSTM ]
        #testlist = [ config.PYTORCHCNN ]
        testlist = [ config.PYTORCHMLP, config.PYTORCHRNN, config.PYTORCHLSTM, config.PYTORCHGRU, config.PYTORCHCNN, config.PYTORCHCNN2 ]

        accuracies = {}
        times = {}
        for test in testlist:
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
