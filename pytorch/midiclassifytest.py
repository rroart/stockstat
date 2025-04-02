import unittest

import config
import midicli as cli

class MyTestCase(unittest.TestCase):
    def test_something(self):
        testlist = [ config.PYTORCHGPTMIDIRPR, config.PYTORCHGPTMIDIRPR, config.PYTORCHGPTMIDIFIGARO ]
        testlist = [ config.PYTORCHGPTMIDIMMT ]
        testlist = [ config.PYTORCHGPTMIDIFIGARO ]
        for test in testlist:
            #result = cli.learn(ds = 'maestro', cf = test, take = 40, steps = 1)
            ds = 'maestro'
            if test == config.PYTORCHGPTMIDIFIGARO:
                ds = 'figaro'
            if test == config.PYTORCHGPTMIDIMMT:
                ds = 'sod'
            #result = cli.learn(ds = ds, cf = test, take = 40, steps = 1)
            #print(result)
            #self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
            result = cli.generate(text ="I like travelling", ds = ds, cf = test, take = 40)
            print(result)
            #self.assertIsNotNone(result['classifyarray'][0], "Text")  # add assertion
        # here

if __name__ == '__main__':
    unittest.main()
