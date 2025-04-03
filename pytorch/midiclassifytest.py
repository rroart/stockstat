import unittest

import config
import midicli as cli

class MyTestCase(unittest.TestCase):
    def test_something(self):
        testmap = { }
        testmap [ config.PYTORCHGPTMIDIRPR ] = ['maestro']
        testmap [ config.PYTORCHGPTMIDI ] = ['maestro']
        testmap [ config.PYTORCHGPTMIDIFIGARO ] = ['lmd_full']
        testmap [ config.PYTORCHGPTMIDIMMT] = [ 'sod', 'lmd_full', 'snd' ]
        testmap [ config.PYTORCHGPTMIDIMMT] = [ 'lmd_full', 'snd' ]
        testmap [ config.PYTORCHGPTMIDIMMT] = [ 'snd' ]
        testlist = [ config.PYTORCHGPTMIDIRPR, config.PYTORCHGPTMIDIRPR, config.PYTORCHGPTMIDIFIGARO ]
        testlist = [ config.PYTORCHGPTMIDIFIGARO ]
        testlist = [ config.PYTORCHGPTMIDIMMT ]
        for test in testlist:
            #result = cli.learn(ds = 'maestro', cf = test, take = 40, steps = 1)
            dslist = testmap[test]
            for ds in dslist:
                result = cli.learn(ds = ds, cf = test, take = 40, steps = 1)
                print(result)
                self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
                result = cli.generate(text ="I like travelling", ds = ds, cf = test, take = 40)
                print(result)
                #self.assertIsNotNone(result['classifyarray'][0], "Text")  # add assertion
        # here

if __name__ == '__main__':
    unittest.main()
